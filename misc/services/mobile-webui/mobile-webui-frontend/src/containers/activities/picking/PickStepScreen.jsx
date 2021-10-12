import React, { Component } from 'react';
import { connect } from 'react-redux';
import { push } from 'connected-react-router';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router';
import counterpart from 'counterpart';

// import { toastError } from '../../../utils/toast';
// import { postQtyPicked } from '../../../api/picking';
import { pushHeaderEntry } from '../../../actions/HeaderActions';
import { updatePickingStepQty } from '../../../actions/PickingActions';
import ButtonWithIndicator from '../../../components/ButtonWithIndicator';
import * as CompleteStatus from '../../../constants/CompleteStatus';
import ScreenToaster from '../../../components/ScreenToaster';

class PickStepScreen extends Component {
  onScanHUButtonClick = () => {
    const {
      wfProcessId,
      activityId,
      lineId,
      stepId,
      stepProps: { huBarcode, qtyToPick }, //, qtyPicked },
      push,
      pushHeaderEntry,
    } = this.props;
    const location = `/workflow/${wfProcessId}/activityId/${activityId}/lineId/${lineId}/stepId/${stepId}/scanner`;

    push(location);
    pushHeaderEntry({
      location,
      values: [
        {
          caption: counterpart.translate('general.Barcode'),
          value: huBarcode,
        },
        {
          caption: counterpart.translate('general.QtyToPick'),
          value: qtyToPick,
        },
        // {
        //   caption: counterpart.translate('general.QtyPicked'),
        //   value: qtyPicked,
        // },
      ],
    });
  };

  // onQtyPickedChanged = (e) => {
  //   const { updatePickingStepQty, wfProcessId, activityId, lineId, stepId } = this.props;
  //   const qtyPicked = e.target.value;
  //   const inputQty = parseInt(qtyPicked);

  //   if (isNaN(inputQty)) {
  //     return;
  //   }

  //   const isValidQty = this.validateQtyInput(inputQty);

  //   if (isValidQty) {
  //     updatePickingStepQty({ wfProcessId, activityId, lineId, stepId, qtyPicked });
  //     postQtyPicked({ wfProcessId, activityId, stepId, qtyPicked });
  //     // TODO: handle the promise
  //   } else {
  //     toastError({ messageKey: 'activities.picking.invalidQtyPicked' });
  //   }
  // };

  // validateQtyInput = (numberInput) => {
  //   const {
  //     stepProps: { qtyToPick },
  //   } = this.props;
  //   return numberInput >= 0 && numberInput <= qtyToPick;
  // };

  componentWillUnmount() {
    const {
      stepProps: { qtyPicked },
      wfProcessId,
      activityId,
      lineId,
      stepId,
      updatePickingStepQty,
    } = this.props;
    qtyPicked === '' && updatePickingStepQty({ wfProcessId, activityId, lineId, stepId, qtyPicked: 0 });
  }

  render() {
    const {
      stepProps: { huBarcode, qtyToPick, scannedHUBarcode }, // qtyPicked,  },
    } = this.props;

    const isValidCode = !!scannedHUBarcode;
    const scanButtonCaption = isValidCode
      ? `${scannedHUBarcode}`
      : counterpart.translate('activities.picking.scanHUBarcode');

    const scanButtonStatus = isValidCode ? CompleteStatus.COMPLETED : CompleteStatus.NOT_STARTED;

    return (
      <div className="pt-3 section picking-step-container">
        <div className="picking-step-details centered-text is-size-5">
          <div className="columns is-mobile">
            <div className="column is-half has-text-right has-text-weight-bold pb-0 pl-0 pr-0">
              {counterpart.translate('general.Barcode')}
            </div>
            <div className="column is-half has-text-left pb-0">{huBarcode}</div>
          </div>
          <div className="columns is-mobile">
            <div className="column is-half has-text-right has-text-weight-bold pb-0 pl-0 pr-0">
              {counterpart.translate('general.QtyToPick')}:
            </div>
            <div className="column is-half has-text-left pb-0">{qtyToPick}</div>
          </div>
          {/*          <div className="columns is-mobile">
            <div className="column is-half has-text-right has-text-weight-bold pb-0 pl-0 pr-0">
              {counterpart.translate('general.QtyPicked')}:
            </div>
            <div className="column is-half has-text-left pb-0">
              {isValidCode && <input type="number" value={qtyPicked} onChange={(e) => this.onQtyPickedChanged(e)} />}
              {!isValidCode && qtyPicked}
            </div>
          </div>*/}
          <div className="mt-0">
            <button className="button is-outlined complete-btn" onClick={this.onScanHUButtonClick}>
              <ButtonWithIndicator caption={scanButtonCaption} completeStatus={scanButtonStatus} />
            </button>
          </div>
          <ScreenToaster
            position="bottom-center"
            toastOptions={{
              success: {
                style: {
                  background: 'green',
                },
              },
              error: {
                style: {
                  background: 'red',
                },
              },
            }}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { workflowId: wfProcessId, activityId, lineId, stepId } = ownProps.match.params;
  const stepProps = state.wfProcesses_status[wfProcessId].activities[activityId].dataStored.lines[lineId].steps[stepId];

  return {
    wfProcessId,
    activityId,
    lineId,
    stepId,
    stepProps,
  };
};

PickStepScreen.propTypes = {
  //
  // Props
  wfProcessId: PropTypes.string.isRequired,
  activityId: PropTypes.string.isRequired,
  lineId: PropTypes.string.isRequired,
  stepId: PropTypes.string.isRequired,
  stepProps: PropTypes.object.isRequired,
  //
  // Actions
  updatePickingStepQty: PropTypes.func.isRequired,
  push: PropTypes.func.isRequired,
  pushHeaderEntry: PropTypes.func.isRequired,
};

export default withRouter(connect(mapStateToProps, { updatePickingStepQty, push, pushHeaderEntry })(PickStepScreen));
