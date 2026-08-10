export const subheadings = {
  paymentType: (defendantName: string) => `How do you want ${defendantName} to pay?`,
  paymentType1v2: 'How do you want both defendants to pay?',
};

export const radioButtons = {
  paymentType: {
    label: 'Payment type',
    immediately: {
      label: 'Immediately',
      selector: '#paymentTypeSelection-IMMEDIATELY',
    },
    setDate: {
      label: 'By a set date',
      selector: '#paymentTypeSelection-SET_DATE',
    },
    repaymentPlan: {
      label: 'By repayment plan',
      selector: '#paymentTypeSelection-REPAYMENT_PLAN',
    },
  },
};
