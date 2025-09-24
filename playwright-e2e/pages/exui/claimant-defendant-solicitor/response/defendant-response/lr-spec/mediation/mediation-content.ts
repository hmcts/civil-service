import { Party } from '../../../../../../../models/partys';

export const radioButtons = {
  yesMediation: {
    label: 'Yes',
    selector: (defendantParty: Party) =>
      `#responseClaimMediationSpec${defendantParty.number === 1 ? '' : defendantParty.number}Required_Yes`,
  },
  noMediation: {
    label: 'No',
    selector: (defendantParty: Party) =>
      `#responseClaimMediationSpec${defendantParty.number === 1 ? '' : defendantParty.number}Required_No`,
  },
};
