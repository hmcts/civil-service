export const getDefendantHeading = (defendantNumber: number) =>
  `Certificate of Service [defendant${defendantNumber}]`;

export const subheading = 'Check your answers';

export const table = {
  dateOfService: {
    label: 'On what day did you serve?',
  },
  dateDeemedServed: {
    label: 'The date of service is',
  },
  documentsServed: {
    label: 'What documents did you serve?',
    defendant1Answer: 'Test Documents 1',
    defendant2Answer: 'Test Documents 2',
  },
  notifyClaimRecipient: {
    label: 'Who did you serve the claim to?',
    defendant1Answer: 'Defendant 1',
    defendant2Answer: 'Defendant 2',
  },
  documentsServedLocation: {
    label: 'The location where you served the documents was the:',
    defendant1Answer: 'Test Address 1',
    defendant2Answer: 'Test Address 2',
  },
  serveType: {
    label: 'How did you serve the documents?',
    defendant1Answer: 'Personally handed it to or left it with',
    defendant2Answer: 'Delivered to or left at permitted place',
  },
  locationType: {
    label: 'Select the type of location where you served the documents',
    defendant1Answer: 'Usual Residence',
    defendant2Answer: 'Last known residence',
  },
  name: {
    label: 'Your name',
    defendant1Answer: 'Name 1',
    defendant2Answer: 'Name 2',
  },
  firm: {
    label: 'Your firm',
    defendant1Answer: 'Law firm 1',
    defendant2Answer: 'Law firm 2',
  },
};

export const buttons = {
  submit: {
    label: 'Submit',
    selector: "button[type='submit']",
  },
};
