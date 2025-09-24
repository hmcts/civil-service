import { Party } from '../../../../../../models/partys';

export const radioButtons = {
  fullDefence: {
    label: 'Defend all of the claim',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseIntentionType${claimantParty.number === 2 ? 'Applicant2' : ''}-FULL_DEFENCE`,
  },
  partAdmit: {
    label: 'Defend part of the claim',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseIntentionType${claimantParty.number === 2 ? 'Applicant2' : ''}-PART_DEFENCE`,
  },
  contestJurisdiction: {
    label: 'Contest jurisdiction',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseIntentionType${claimantParty.number === 2 ? 'Applicant2' : ''}-CONTEST_JURISDICTION`,
  },
};
