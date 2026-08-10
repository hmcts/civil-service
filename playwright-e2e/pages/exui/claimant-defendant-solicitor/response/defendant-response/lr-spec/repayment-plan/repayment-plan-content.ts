export const radioButtons = {
  repaymentFrequency: {
    label: 'How often will your client make payments?',
    everyWeek: {
      label: 'Every week',
      selector: '#respondent1RepaymentPlan_repaymentFrequency-ONCE_ONE_WEEK',
    },
    everyTwoWeeks: {
      label: 'Every 2 weeks',
      selector: '#respondent1RepaymentPlan_repaymentFrequency-ONCE_TWO_WEEKS',
    },
    everyMonth: {
      label: 'Every month',
      selector: '#respondent1RepaymentPlan_repaymentFrequency-ONCE_ONE_MONTH',
    },
  },
};

export const inputs = {
  paymentAmount: {
    label: 'Regular payments of',
    selector: '#respondent1RepaymentPlan_paymentAmount',
  },
  firstRepaymentDate: {
    label: 'When will your client make the first payment?',
    selectorKey: 'firstRepaymentDate',
  },
};
