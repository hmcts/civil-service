export const headings = {
  setAside: 'Set aside judgment',
  judgmentDetails: 'Judgment details',
};

export const inputs = {
  orderDate: {
    label: 'Enter the date of the order setting aside the judgment',
    selectorKey: 'joSetAsideOrderDate',
  },
  applicationDate: {
    label: 'Enter the date of the application to set aside',
    selectorKey: 'joSetAsideApplicationDate',
  },
  defenceReceivedDate: {
    label: 'Enter the date of the defence was received',
    selectorKey: 'joSetAsideDefenceReceivedDate',
  },
};

export const radioButtons = {
  orderAfterApplication: {
    label: 'Order following an application to set aside',
    selector: '#joSetAsideOrderType-ORDER_AFTER_APPLICATION',
  },
  orderAfterDefence: {
    label: 'Order following defence received',
    selector: '#joSetAsideOrderType-ORDER_AFTER_DEFENCE',
  },
};
