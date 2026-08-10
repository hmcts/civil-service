export const subheadings = {
  claimTimeline: 'Claim timeline',
};

export const paragraphs = {
  dateInfo: 'If you do not know the exact date, tell us the month and year.',
};

export const buttons = {
  addNew: { title: 'Add new', selector: "button[class='button write-collection-add-item__top']" },
};

export const inputs = {
  timelineDate: {
    selectorKey: 'timelineDate',
  },
  timelineDescription: {
    label: 'What happened',
    selector: (eventNumber: number) => `#timelineOfEvents_${eventNumber - 1}_timelineDescription`,
  },
};
