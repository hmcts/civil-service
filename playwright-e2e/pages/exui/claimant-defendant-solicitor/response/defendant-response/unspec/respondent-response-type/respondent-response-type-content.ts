import { Party } from '../../../../../../../models/partys';

export const radioButtons = {
  rejectAll: {
    label: 'Reject all of the claim',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#respondent${defendantParty.number}ClaimResponseType${claimantParty.number === 2 ? 'ToApplicant2' : ''}-FULL_DEFENCE`,
  },
  admitAll: {
    label: 'Admit all of the claim',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#respondent${defendantParty.number}ClaimResponseType${claimantParty.number === 2 ? 'ToApplicant2' : ''}-FULL_ADMISSION`,
  },
  partAdmit: {
    label: 'Admit part of the claim',
    selector: (defendantParty: Party, isClaimant2 = false) =>
      `#respondent${defendantParty.number}ClaimResponseType${isClaimant2 ? 'ToApplicant2' : ''}-PART_ADMISSION`,
  },
  counterClaim: {
    label: 'Reject all of the claim and wants to counterclaim',
    selector: (defendantParty: Party, claimantParty: Party) =>
      `#respondent${defendantParty.number}ClaimResponseType${claimantParty.number === 2 ? 'ToApplicant2' : ''}-COUNTER_CLAIM`,
  },
};
