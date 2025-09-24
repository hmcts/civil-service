import { Party } from '../../../../models/partys';

export const radioButtons = {
  individual: {
    label: 'Individual',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}_type-INDIVIDUAL`,
  },
  company: {
    label: 'Company',
    selector: (claimantDefendantParty: Party) => `#${claimantDefendantParty.oldKey}_type-COMPANY`,
  },
  organisation: {
    label: 'Organisation',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}_type-ORGANISATION`,
  },
  soleTrader: {
    label: 'Sole trader',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}_type-SOLE_TRADER`,
  },
};
