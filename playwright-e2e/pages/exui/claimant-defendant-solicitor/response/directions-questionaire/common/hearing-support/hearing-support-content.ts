import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  supportNeeds: 'Support with access needs',
};

export const radioButtons = {
  supportRequirements: {
    label: 'Does anyone require support for a court hearing?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearingSupport_supportRequirements_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearingSupport_supportRequirements_No`,
    },
  },
};

export const inputs = {
  supportRequirementsAdditional: {
    label:
      'Please name all people who need support and the kind of support they will need. For example, Jane Smith: requires wheelchair access.',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQHearingSupport_supportRequirementsAdditional`,
  },
};
