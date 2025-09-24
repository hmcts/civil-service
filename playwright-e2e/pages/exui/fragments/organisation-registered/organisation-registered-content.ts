import { Party } from '../../../../models/partys';

export const radioButtons = {
  organisationRegistered: {
    label: 'Is the organisation registered with MyHMCTS?',
    yes: {
      label: 'Yes',
      selector: (defendantParty: Party) => `#${defendantParty.oldKey}OrgRegistered_Yes`,
    },
    no: {
      label: 'No',
      selector: (defendantParty: Party) => `#${defendantParty.oldKey}OrgRegistered_Yes`,
    },
  },
};
