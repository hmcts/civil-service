import { ClaimantDefendantPartyType } from '../claimant-defendant-party-types';

export type CaseFlagLocation = string;

type CaseFlagLocations = {
  CASE_LEVEL: CaseFlagLocation;
  CLAIMANT_1: (claimant1PartyType: ClaimantDefendantPartyType) => CaseFlagLocation;
  CLAIMANT_1_LITIGATION_FRIEND: CaseFlagLocation;
  CLAIMANT_2: (claimant1PartyType: ClaimantDefendantPartyType) => CaseFlagLocation;
  CLAIMANT_2_LITIGATION_FRIEND: CaseFlagLocation;
  DEFENDANT_1: (claimant1PartyType: ClaimantDefendantPartyType) => CaseFlagLocation;
  DEFENDANT_1_LITIGATION_FRIEND: CaseFlagLocation;
  DEFENDANT_2: (claimant1PartyType: ClaimantDefendantPartyType) => CaseFlagLocation;
  DEFENDANT_2_LITIGATION_FRIEND: CaseFlagLocation;
  CLAIMANT_EXPERT_1: CaseFlagLocation;
  CLAIMANT_EXPERT_2: CaseFlagLocation;
  CLAIMANT_WITNESS_1: CaseFlagLocation;
  CLAIMANT_WITNESS_2: CaseFlagLocation;
  DEFENDANT_1_EXPERT_1: CaseFlagLocation;
  DEFENDANT_1_EXPERT_2: CaseFlagLocation;
  DEFENDANT_1_WITNESS_1: CaseFlagLocation;
  DEFENDANT_1_WITNESS_2: CaseFlagLocation;
  DEFENDANT_2_EXPERT_1: CaseFlagLocation;
  DEFENDANT_2_EXPERT_2: CaseFlagLocation;
  DEFENDANT_2_WITNESS_1: CaseFlagLocation;
  DEFENDANT_2_WITNESS_2: CaseFlagLocation;
};

export default CaseFlagLocations;
