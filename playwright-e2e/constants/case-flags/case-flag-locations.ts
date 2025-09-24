import CaseDataHelper from '../../helpers/case-data-helper';
import CaseFlagLocations from '../../models/case-flags/case-flag-locations';
import { ClaimantDefendantPartyType } from '../../models/claimant-defendant-party-types';
import partys from '../partys';

const caseFlagLocations: CaseFlagLocations = {
  CASE_LEVEL: 'Case level',
  CLAIMANT_1: (claimant1PartyType: ClaimantDefendantPartyType) =>
    `${
      CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType).partyName
    } (Claimant 1)`,
  CLAIMANT_1_LITIGATION_FRIEND: `${CaseDataHelper.buildLitigationFriendData(partys.CLAIMANT_1_LITIGATION_FRIEND).partyName} (Claimant 1 Litigation Friend)`,
  CLAIMANT_2: (claimant2PartyType: ClaimantDefendantPartyType) =>
    `${
      CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_2, claimant2PartyType).partyName
    } (Claimant 2)`,
  CLAIMANT_2_LITIGATION_FRIEND: `${CaseDataHelper.buildLitigationFriendData(partys.CLAIMANT_2_LITIGATION_FRIEND).partyName} (Claimant 2 Litigation Friend)`,
  DEFENDANT_1: (defendant1PartyType: ClaimantDefendantPartyType) =>
    `${
      CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1PartyType)
        .partyName
    } (Defendant 1)`,
  DEFENDANT_1_LITIGATION_FRIEND: `${CaseDataHelper.buildLitigationFriendData(partys.DEFENDANT_1_LITIGATION_FRIEND).partyName} (Defendant 1 Litigation Friend)`,
  DEFENDANT_2: (defendant2PartyType: ClaimantDefendantPartyType) =>
    `${
      CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_2, defendant2PartyType)
        .partyName
    } (Defendant 2)`,
  DEFENDANT_2_LITIGATION_FRIEND: `${CaseDataHelper.buildLitigationFriendData(partys.DEFENDANT_2_LITIGATION_FRIEND).partyName} (Defendant 2 Litigation Friend)`,
  CLAIMANT_EXPERT_1: `${CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_1).partyName} (Claimant solicitor expert)`,
  CLAIMANT_EXPERT_2: `${CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_2).partyName} (Claimant solicitor expert)`,
  CLAIMANT_WITNESS_1: `${CaseDataHelper.buildWitnessData(partys.CLAIMANT_WITNESS_1).partyName} (Claimant solicitor witness)`,
  CLAIMANT_WITNESS_2: `${CaseDataHelper.buildWitnessData(partys.CLAIMANT_WITNESS_1).partyName} (Claimant solicitor witness)`,
  DEFENDANT_1_EXPERT_1: `${CaseDataHelper.buildExpertData(partys.DEFENDANT_1_EXPERT_1).partyName} (Defendant solicitor 1 expert)`,
  DEFENDANT_1_EXPERT_2: `${CaseDataHelper.buildExpertData(partys.DEFENDANT_1_EXPERT_2).partyName} (Defendant solicitor 1 expert)`,
  DEFENDANT_1_WITNESS_1: `${CaseDataHelper.buildWitnessData(partys.DEFENDANT_1_WITNESS_1).partyName} (Defendant solicitor 1 witness)`,
  DEFENDANT_1_WITNESS_2: `${CaseDataHelper.buildWitnessData(partys.DEFENDANT_1_WITNESS_2).partyName} (Defendant solicitor 2 witness)`,
  DEFENDANT_2_EXPERT_1: `${CaseDataHelper.buildExpertData(partys.DEFENDANT_2_EXPERT_1).partyName} (Defendant solicitor 2 expert)`,
  DEFENDANT_2_EXPERT_2: `${CaseDataHelper.buildExpertData(partys.DEFENDANT_2_EXPERT_2).partyName} (Defendant solicitor 2 expert)`,
  DEFENDANT_2_WITNESS_1: `${CaseDataHelper.buildWitnessData(partys.DEFENDANT_2_WITNESS_1).partyName} (Defendant solicitor 2 witness)`,
  DEFENDANT_2_WITNESS_2: `${CaseDataHelper.buildWitnessData(partys.DEFENDANT_2_WITNESS_2).partyName} (Defendant solicitor 2 witness)`,
};

export default caseFlagLocations;
