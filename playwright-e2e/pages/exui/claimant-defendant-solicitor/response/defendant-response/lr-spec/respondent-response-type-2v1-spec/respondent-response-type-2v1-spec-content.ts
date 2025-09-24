import { Party } from '../../../../../../../models/partys';

export const radioButtons = {
  fullDefence: {
    label: 'Defends all of the claim',
    selector: (claimantParty: Party) =>
      `#${claimantParty.key}ClaimResponseTypeForSpec-FULL_DEFENCE`,
  },
  fullAdmit: {
    label: 'Admits all of the claim',
    selector: (claimantParty: Party) =>
      `#${claimantParty.key}ClaimResponseTypeForSpec-FULL_ADMISSION`,
  },
  partAdmit: {
    label: 'Admits part of the claim',
    selector: (claimantParty: Party) =>
      `#${claimantParty.key}ClaimResponseTypeForSpec-PART_ADMISSION`,
  },
  counterClaim: {
    label: 'Defends all of the claim and wants to counterclaim',
    selector: (claimantParty: Party) =>
      `#${claimantParty.key}ClaimResponseTypeForSpec-COUNTER_CLAIM`,
  },
};
