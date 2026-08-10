export const subheadings = {
  instalments: (defendantName: string) => `Suggest instalments for ${defendantName}`,
  instalments1v2: 'Suggest instalments for both defendants',
};

export const paragraphs = {
  claimAmount: 'Total claim amount is',
};

export const inputs = {
  regularPayments: {
    label: 'Regular payments of',
    hint: 'For example, £10',
    selector: '#repaymentSuggestion',
  },
  firstInstalmentDate: {
    label: 'Date for first instalment',
    hint: 'This must be after',
    selectorKey: 'repaymentDate',
  },
};

export const radioButtons = {
  howOften: {
    label: 'How often do you want to receive payments?',
    everyWeek: {
      label: 'Every week',
      selector: '#repaymentFrequency-ONCE_ONE_WEEK',
    },
    every2Weeks: {
      label: 'Every 2 weeks',
      selector: '#repaymentFrequency-ONCE_TWO_WEEKS',
    },
    everyMonth: {
      label: 'Every month',
      selector: '#repaymentFrequency-ONCE_ONE_MONTH',
    },
  },
};
