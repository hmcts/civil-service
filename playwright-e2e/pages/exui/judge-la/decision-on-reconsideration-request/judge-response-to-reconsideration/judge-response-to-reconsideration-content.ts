export const subheadings = {
  createNewSDO: 'No, create a new SDO',
  createGeneralOrder: 'No, previous order needs amending',
};

export const containers = {
  createNewSDO: '#judgeResponseToReconsiderationDetailsNo_judgeResponseToReconsiderationDetailsNo',
  createGeneralOrder:
    '#judgeResponseToReconsiderationDetailsAmending_judgeResponseToReconsiderationDetailsAmending',
};

export const radioButtons = {
  yes: {
    label: 'Yes',
    selector: '#decisionOnRequestReconsiderationOptions-YES',
  },
  noCreateNewSdo: {
    label: 'No, create a new SDO',
    selector: '#decisionOnRequestReconsiderationOptions-CREATE_SDO',
  },
  noCreateGeneralOrder: {
    label: 'No, previous order needs amending',
    selector: '#decisionOnRequestReconsiderationOptions-CREATE_GENERAL_ORDER',
  },
};

export const inputs = {
  reason: {
    label: 'Reason for upholding previous order',
    hintText: 'State the reason why previous order is upheld (i.e. previous order doesn\'t need amending, all information on the claim has been taken into account and directions order drawn are all in order)',
    selector: '#upholdingPreviousOrderReason_reasonForReconsiderationTxtYes',
  },
};

export const paragraphs = {
  createNewSDO:
    'If you select this option, a new SDO task for this case is created and appears in "Available Tasks" on the main dashboard. You will need to go there to re-select the case to continue.',
  createGeneralOrder:
    'If you wish to do this you should select "Make an order" from dropdown menu on the right of the screen. This will allow you to make a bespoke order in the case.',
};
