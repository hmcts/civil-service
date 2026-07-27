export const subheadings = {
  interestClaimFrom: 'When are you claiming interest from?',
};

export const radioButtons = {
  interestClaimFrom: {
    submitDate: {
      label:
        'The date you submit the claim. The interest will then be calculated up until the claim is settled or a judgment has been made.',
      selector: '#interestClaimFrom-FROM_CLAIM_SUBMIT_DATE',
    },
    specificDate: {
      label:
        'A specific date. For example, the date an invoice was overdue, or the date that you told someone they owed you money.',
      selector: '#interestClaimFrom-FROM_A_SPECIFIC_DATE',
    },
  },
};
