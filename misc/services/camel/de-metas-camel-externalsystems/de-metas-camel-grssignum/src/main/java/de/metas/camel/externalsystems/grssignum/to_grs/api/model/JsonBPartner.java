/*
 * #%L
 * de-metas-camel-grssignum
 * %%
 * Copyright (C) 2021 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

package de.metas.camel.externalsystems.grssignum.to_grs.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;

@Value
@JsonDeserialize(builder = JsonBPartner.JsonBPartnerBuilder.class)
public class JsonBPartner
{
	@NonNull
	@JsonProperty("FLAG")
	Integer flag;

	@NonNull
	@JsonProperty("MKREDID")
	String id;

	@NonNull
	@JsonProperty("KURZBEZEICHNUNG")
	String name;

	@JsonProperty("NAMENSZUSATZ")
	String name2;

	@JsonProperty("INAKTIV")
	Integer inactiveBit;

	@JsonProperty("MID")
	String tenantId;

	@JsonProperty("ADRESSE 1")
	String address1;

	@JsonProperty("ADRESSE 2")
	String address2;

	@JsonProperty("ADRESSE 3")
	String address3;

	@JsonProperty("ADRESSE 4")
	String address4;

	@JsonProperty("PLZ")
	String postal;

	@JsonProperty("ORT")
	String city;

	@JsonProperty("LANDESCODE")
	String countryCode;

	@JsonProperty("GLN")
	String gln;

	@JsonProperty("KONTAKTE")
	List<JsonBPartnerContact> contacts;

	@Builder
	public JsonBPartner(
			@JsonProperty("FLAG") final @NonNull Integer flag,
			@JsonProperty("MKREDID") final @NonNull String id,
			@JsonProperty("KURZBEZEICHNUNG") final @NonNull String name,
			@JsonProperty("INAKTIV") final int inactive,
			@JsonProperty("MID") final @Nullable String tenantId,
			@JsonProperty("NAMENSZUSATZ") final @Nullable String name2,
			@JsonProperty("ADRESSE 1") final @Nullable String address1,
			@JsonProperty("ADRESSE 2") final @Nullable String address2,
			@JsonProperty("ADRESSE 3") final @Nullable String address3,
			@JsonProperty("ADRESSE 4") final @Nullable String address4,
			@JsonProperty("PLZ") final @Nullable String postal,
			@JsonProperty("ORT") final @Nullable String city,
			@JsonProperty("LANDESCODE") final @Nullable String countryCode,
			@JsonProperty("GLN") final @Nullable String gln,
			@JsonProperty("KONTAKTE") final @Nullable @Singular List<JsonBPartnerContact> contacts)
	{
		this.flag = flag;
		this.id = id;
		this.name = name;
		this.inactiveBit = inactive;
		this.tenantId = tenantId;
		this.name2 = name2;
		this.address1 = address1;
		this.address2 = address2;
		this.address3 = address3;
		this.address4 = address4;
		this.postal = postal;
		this.city = city;
		this.countryCode = countryCode;
		this.gln = gln;
		this.contacts = contacts;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@JsonPOJOBuilder(withPrefix = "")
	public static class JsonBPartnerBuilder
	{
	}

	@JsonIgnore
	public boolean isActive()
	{
		return inactiveBit != 1;
	}
}
