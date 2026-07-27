export const heading = 'Length, date and priority level of hearing';

export const inputs = {
  lengthOfHearing: {
    days: {
      label: 'Days',
      selector: '#durationdays',
    },
    hours: {
      label: 'Hours',
      selector: '#durationhours',
      value1: '2',
      value2: '3',
    },
    minutes: {
      label: 'Minutes',
      selector: '#durationmins',
    },
  },
};

export const radioButtons = {
  specificDate: {
    label: 'Does the hearing need to take place on a specific date?',
    no: {
      label: 'No',
      selector: '#noSpecificDate',
    },
    yes: {
      label: 'Yes',
      selector: '#hearingSingleDate',
    },
    chooseDateRange: {
      label: 'Choose a date range',
      selector: '#hearingDateRange',
    },
  },
  priorityOfHearing: {
    label: 'What is the priority of this hearing?',
    standard: {
      label: 'Standard',
      selector: '#Standard',
    },
    urgent: {
      label: 'Urgent',
      selector: '#Urgent',
    },
  },
};
