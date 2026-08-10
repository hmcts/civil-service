export const radioButtons = {
  drawDirectionsOrder: {
    label: 'Do you wish to enter judgment for a sum of damages to be decided ?',
    yes: {
      label: 'Yes',
      selector: '#drawDirectionsOrderRequired_Yes',
    },
    no: {
      label: 'No',
      selector: '#drawDirectionsOrderRequired_No',
    },
  },
};

export const inputs = {
  judgementSum: {
    label: 'Judgment for the claimant for a sum to be decided by the court (Optional)',
    hintText: 'Subject to a deduction of the percentage below',
    selector: '#drawDirectionsOrder_judgementSum',
  },
};
