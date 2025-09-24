import ClaimantDefendantPartyTypes from '../models/claimant-defendant-party-types';

const claimantDefendantPartyTypes: ClaimantDefendantPartyTypes = {
  INDIVIDUAL: {
    type: 'INDIVIDUAL',
    key: 'individual',
  },
  COMPANY: {
    type: 'COMPANY',
    key: 'company',
  },
  SOLE_TRADER: {
    type: 'SOLE_TRADER',
    key: 'soleTrader',
  },
  ORGANISATION: {
    type: 'ORGANISATION',
    key: 'organisation',
  },
};

export default claimantDefendantPartyTypes;
