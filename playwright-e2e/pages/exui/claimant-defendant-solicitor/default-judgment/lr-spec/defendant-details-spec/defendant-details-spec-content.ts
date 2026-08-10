export const radioButtons = {
  selectDefendant: {
    label: 'Against which Defendant are you requesting default judgment?',
    defendant: {
      label: (defendantName: string) => defendantName,
    },
    both: {
      label: 'Both Defendants',
    },
  },
};
