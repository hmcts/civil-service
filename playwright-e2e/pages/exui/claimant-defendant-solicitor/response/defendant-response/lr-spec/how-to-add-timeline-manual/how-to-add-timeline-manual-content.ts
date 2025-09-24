import { Party } from '../../../../../../../models/partys';

export const heading = 'Add timeline of events (Optional)';

export const buttons = {
  addNew: {
    title: 'Add new',
    selector: '.write-collection-add-item__top',
  },
};

export const inputs = {
  timelineEvent: {
    date: {
      label: 'Date (Optional)',
      selectorKey: 'timelineDate',
    },
    eventDescription: {
      label: 'What happened (Optional)',
      selector: (defendantParty: Party, eventIndex: number) =>
        `#specResponseTimelineOfEvents${defendantParty.number === 1 ? '' : defendantParty.number}_${eventIndex}_timelineDescription`,
    },
  },
};
