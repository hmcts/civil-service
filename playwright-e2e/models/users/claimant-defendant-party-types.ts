export type ClaimantDefendantPartyType = {
  type: string;
  key: string;
};

type ClaimantDefendantPartyTypes = {
  INDIVIDUAL: ClaimantDefendantPartyType;
  COMPANY: ClaimantDefendantPartyType;
  SOLE_TRADER: ClaimantDefendantPartyType;
  ORGANISATION: ClaimantDefendantPartyType;
};

export default ClaimantDefendantPartyTypes;
