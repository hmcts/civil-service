import { Party } from '../../../../../../../models/partys';

export const radioButtons = {
  fullDefence: {
    label: 'Defends all of the claim',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseTypeForSpec-FULL_DEFENCE`,
  },
  fullAdmit: {
    label: 'Admits all of the claim',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseTypeForSpec-FULL_ADMISSION`,
  },
  partAdmit: {
    label: 'Admits part of the claim',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseTypeForSpec-PART_ADMISSION`,
  },
  counterClaim: {
    label: 'Defends all of the claim and wants to counterclaim',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}ClaimResponseTypeForSpec-COUNTER_CLAIM`,
  },
};
