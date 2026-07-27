import ClaimantDefendantPartyTypes from '../../models/users/claimant-defendant-party-types';

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
