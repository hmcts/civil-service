export const heading = 'Create a hearing notice';

export const dropdowns = {
  venue: {
    label: 'Venue',
    selector: '#hearingLocation',
  },
  startTime: {
    label: 'Start time',
    selector: '#hearingTimeHourMinute',
  },
  duration: {
    label: 'Duration',
    selector: '#hearingDuration',
  }
}

export const radioButtons = {
  inPerson: {
    label: 'In Person',
    selector: '#channel-IN_PERSON'
  },
  video: {
    label: 'Video',
    selector: '#channel-VIDEO'
  },
  telephone: {
    label: 'Telephone',
    selector: '#channel-TELEPHONE'
  }
};

export const inputs = {
  date: {
    selectorKey: 'hearingDate',
  }
};
