export const subheadings = {
  claimInterestOptions: 'How do you want to claim interest?',
};

export const radioButtons = {
  claimInterestOptions: {
    sameRateInterest: {
      label: 'Same rate for whole period of time',
      selector: '#interestClaimOptions-SAME_RATE_INTEREST',
    },
    breakDownInterest: {
      label:
        'Break down interest for different periods of time, or items.' +
        'You can only use this service if any claim for interest is made at the same rate and from the same date.' +
        'To claim interest at different rates or for different periods of time, you should issue your claim on paper',
      selector: '#interestClaimOptions-BREAK_DOWN_INTEREST',
    },
  },
};
