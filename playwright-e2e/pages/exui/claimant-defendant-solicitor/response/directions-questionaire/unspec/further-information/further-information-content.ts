import { Party } from '../../../../../../../models/partys';

export const subheadings = { furtherInformation: 'Further information' };

export const radioButtons = {
  yes: {
    label: 'Yes',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQFurtherInformation_futureApplications_Yes`,
  },
  no: {
    label: 'No',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQFurtherInformation_futureApplications_No`,
  },
};

export const form = {
  label:
    'Please provide any further information the Judge may need, including if you do not agree with the provisional track allocation of this claim (Optional)',
  whatForForm: {
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQFurtherInformation_reasonForFutureApplications`,
  },
  furtherInformationForm: {
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQFurtherInformation_otherInformationForJudge`,
  },
};
