export const heading = 'Make a request for judgment';

export const subheadings = {
  hearingType: 'How would you like the court to decide the amount of damages?',
  draftOrderDirections: 'What directions would you like the court to give?',
};

export const paragraphs = {
  descriptionText:
    'While your preference will be taken into account, the court will make the final decision',
  descriptionDisposalHearing:
    'This is a hearing that lasts a maximum of 30 minutes, where information is presented only on paper, with no spoken evidence.',
  descriptionTrial:
    'This is a hearing which lasts longer than 30 minutes, where the court will receive evidence on paper and listen to any spoken evidence that you wish it to consider when deciding the amount of damages.',
};

export const radioButtons = {
  hearingType: {
    label: 'Hearing type',
    disposalHearing: {
      label: 'Disposal hearing',
      selector: '#hearingSelection-DISPOSAL_HEARING',
    },
    trial: {
      label: 'A trial to decide the amount of damages',
      selector: '#hearingSelection-TRIAL_HEARING',
    },
  },
};

export const inputs = {
  draftOrder: {
    label: 'Draft order',
    selector: '#detailsOfDirection',
  },
};
