package de.metas.manufacturing.workflows_api.activity_handlers;

import com.google.common.collect.ImmutableList;
import de.metas.bpartner.BPartnerId;
import de.metas.bpartner.service.IBPartnerBL;
import de.metas.handlingunits.HUPIItemProductId;
import de.metas.handlingunits.HuPackingInstructionsId;
import de.metas.handlingunits.HuPackingInstructionsItemId;
import de.metas.handlingunits.IHUPIItemProductDAO;
import de.metas.handlingunits.IHandlingUnitsBL;
import de.metas.handlingunits.model.I_M_HU_PI;
import de.metas.handlingunits.model.I_M_HU_PI_Item;
import de.metas.handlingunits.model.I_M_HU_PI_Item_Product;
import de.metas.handlingunits.model.X_M_HU_PI_Version;
import de.metas.manufacturing.job.model.FinishedGoodsReceiveLine;
import de.metas.manufacturing.job.model.ManufacturingJob;
import de.metas.manufacturing.workflows_api.activity_handlers.json.JsonAggregateToNewLU;
import de.metas.manufacturing.workflows_api.activity_handlers.json.JsonAggregateToNewLUList;
import de.metas.manufacturing.workflows_api.activity_handlers.json.JsonFinishedGoodsReceiveLine;
import de.metas.product.IProductBL;
import de.metas.product.ProductId;
import de.metas.util.Services;
import de.metas.workflow.rest_api.controller.v2.json.JsonOpts;
import de.metas.workflow.rest_api.model.UIComponent;
import de.metas.workflow.rest_api.model.UIComponentType;
import de.metas.workflow.rest_api.model.WFActivity;
import de.metas.workflow.rest_api.model.WFActivityStatus;
import de.metas.workflow.rest_api.model.WFActivityType;
import de.metas.workflow.rest_api.model.WFProcess;
import de.metas.workflow.rest_api.service.WFActivityHandler;
import lombok.NonNull;
import org.adempiere.util.api.Params;
import org.compiere.util.Env;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Component
public class MaterialReceiptActivityHandler implements WFActivityHandler
{
	public static final WFActivityType HANDLED_ACTIVITY_TYPE = WFActivityType.ofString("manufacturing.materialReceipt");
	private static final UIComponentType COMPONENT_TYPE = UIComponentType.ofString("manufacturing/materialReceipt");

	private final IHandlingUnitsBL handlingUnitsBL = Services.get(IHandlingUnitsBL.class);
	private final IHUPIItemProductDAO huPIItemProductDAO = Services.get(IHUPIItemProductDAO.class);
	private final IProductBL productBL = Services.get(IProductBL.class);
	private final IBPartnerBL bpartnerBL;

	public MaterialReceiptActivityHandler(
			@NonNull final IBPartnerBL bpartnerBL)
	{
		this.bpartnerBL = bpartnerBL;
	}

	@Override
	public WFActivityType getHandledActivityType() {return HANDLED_ACTIVITY_TYPE;}

	@Override
	public UIComponent getUIComponent(final @NonNull WFProcess wfProcess, final @NonNull WFActivity wfActivity, final @NonNull JsonOpts jsonOpts)
	{
		final ManufacturingJob job = wfProcess.getDocumentAs(ManufacturingJob.class);
		final ImmutableList<JsonFinishedGoodsReceiveLine> lines = job.getActivityById(wfActivity.getId())
				.getFinishedGoodsReceiveAssumingNotNull()
				.streamLines()
				.map(line -> toJson(line, job.getCustomerId(), jsonOpts))
				.collect(ImmutableList.toImmutableList());

		return UIComponent.builder()
				.type(COMPONENT_TYPE)
				.properties(Params.builder()
						.valueObj("lines", lines)
						.build())
				.build();
	}

	private JsonFinishedGoodsReceiveLine toJson(
			@NonNull final FinishedGoodsReceiveLine line,
			@Nullable final BPartnerId customerId,
			@NonNull final JsonOpts jsonOpts)
	{
		final JsonAggregateToNewLUList availablePackingMaterials = getAvailablePackingMaterials(line.getProductId(), customerId);

		return JsonFinishedGoodsReceiveLine.of(line, availablePackingMaterials, jsonOpts);
	}

	@NonNull
	private JsonAggregateToNewLUList getAvailablePackingMaterials(@NonNull final ProductId productId, final @Nullable BPartnerId customerId)
	{
		final List<I_M_HU_PI_Item_Product> tuPIItemProducts = huPIItemProductDAO.retrieveTUs(Env.getCtx(), productId, customerId, false);
		if (tuPIItemProducts.isEmpty())
		{
			return JsonAggregateToNewLUList.emptyBecause("No CU/TU associations found for "
					+ productBL.getProductName(productId)
					+ " and " + (customerId != null ? bpartnerBL.getBPartnerName(customerId) : "any customer"));
		}

		final ArrayList<JsonAggregateToNewLU> availablePackingMaterials = new ArrayList<>();
		final ArrayList<String> debugMessages = new ArrayList<>();
		for (final I_M_HU_PI_Item_Product tuPIItemProduct : tuPIItemProducts)
		{
			final HuPackingInstructionsItemId tuPackingInstructionsItemId = HuPackingInstructionsItemId.ofRepoId(tuPIItemProduct.getM_HU_PI_Item_ID());
			final HuPackingInstructionsId tuPackingInstructionsId = handlingUnitsBL.getPackingInstructionsId(tuPackingInstructionsItemId);

			final List<I_M_HU_PI_Item> luPackingInstructionsItems = handlingUnitsBL.retrieveParentPIItemsForParentPI(
					tuPackingInstructionsId,
					X_M_HU_PI_Version.HU_UNITTYPE_LoadLogistiqueUnit,
					customerId);

			if (!luPackingInstructionsItems.isEmpty())
			{
				for (final I_M_HU_PI_Item luPackingInstructionsItem : luPackingInstructionsItems)
				{
					final I_M_HU_PI luPackingInstructions = handlingUnitsBL.getPI(luPackingInstructionsItem);
					availablePackingMaterials.add(
							JsonAggregateToNewLU.builder()
									.caption(luPackingInstructions.getName())
									.tuCaption(tuPIItemProduct.getName())
									.luPIItemId(HuPackingInstructionsId.ofRepoId(luPackingInstructionsItem.getM_HU_PI_Item_ID()).getRepoId())
									.tuPIItemProductId(HUPIItemProductId.ofRepoId(tuPIItemProduct.getM_HU_PI_Item_Product_ID()).getRepoId())
									.build());
				}
			}
			else
			{
				debugMessages.add("Ignoring " + tuPackingInstructionsId + " (" + tuPIItemProduct + ") because it has no LU PI Items");
			}
		}

		if (availablePackingMaterials.isEmpty())
		{
			return JsonAggregateToNewLUList.emptyBecause("None of the TUs found are assigned to an LU", debugMessages);
		}
		else
		{
			return JsonAggregateToNewLUList.ofList(availablePackingMaterials, debugMessages);
		}
	}

	@Override
	public WFActivityStatus computeActivityState(final WFProcess wfProcess, final WFActivity wfActivity)
	{
		return wfActivity.getStatus();
	}
}
