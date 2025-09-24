import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  howToAddTimeline: 'How do you want to add the claim timeline?',
};

export const radioButtons = {
  upload: {
    label: 'Upload claim timeline template',
    selector: (defendantParty: Party) =>
      `#specClaimResponseTimelineList${defendantParty.number === 1 ? '' : defendantParty.number}-UPLOAD`,
  },
  manual: {
    label: 'Add manually',
    selector: (defendantParty: Party) =>
      `#specClaimResponseTimelineList${defendantParty.number === 1 ? '' : defendantParty.number}-MANUAL`,
  },
};
