import { Party } from '../../../../models/partys';
import { ClaimantDefendantPartyType } from '../../../../models/claimant-defendant-party-types';

export const inputs = {
  name: {
    label: 'Name',
    selector: (
      claimantDefendantParty: Party,
      claimantDefendantPartyType: ClaimantDefendantPartyType,
    ) => `#${claimantDefendantParty.oldKey}_${claimantDefendantPartyType.key}Name`,
  },
  title: {
    label: 'Title (Optional)',
    selector: (
      claimantDefendantParty: Party,
      claimantDefendantPartyType: ClaimantDefendantPartyType,
    ) => `#${claimantDefendantParty.oldKey}_${claimantDefendantPartyType.key}Title`,
  },
  tradingAs: {
    label: 'Trading as',
    selector: (
      claimantDefendantParty: Party,
      claimantDefendantPartyType: ClaimantDefendantPartyType,
    ) => `#${claimantDefendantParty.oldKey}_${claimantDefendantPartyType.key}TradingAs`,
  },
  firstName: {
    label: 'First name',
    selector: (
      claimantDefendantParty: Party,
      claimantDefendantPartyType: ClaimantDefendantPartyType,
    ) => `#${claimantDefendantParty.oldKey}_${claimantDefendantPartyType.key}FirstName`,
  },
  lastName: {
    label: 'Last name',
    selector: (
      claimantDefendantParty: Party,
      claimantDefendantPartyType: ClaimantDefendantPartyType,
    ) => `#${claimantDefendantParty.oldKey}_${claimantDefendantPartyType.key}LastName`,
  },
  dateOfBirth: {
    label: 'Date of birth',
  },
  email: {
    label: 'Email (Optional)',
    selector: (claimantDefendantParty: Party) => `#${claimantDefendantParty.oldKey}_partyEmail`,
  },
  phone: {
    label: 'Phone (Optional)',
    selector: (claimantDefendantParty: Party) => `#${claimantDefendantParty.oldKey}_partyPhone`,
  },
};
