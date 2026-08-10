import partys from '../../../../../constants/users/partys';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';

export const radioButtons = {
  party: {
    label: 'Select party',
    claimant1: {
      label: (claimantPartyType: ClaimantDefendantPartyType) =>
      `CLAIMANT 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimantPartyType).partyName}`,
      selector: '#partyChosen_CLAIMANT_1',
    },
    claimant1Litigation: {
      label: () => `CLAIMANT 1: Litigation Friend: ${CaseDataHelper.buildLitigationFriendData(partys.CLAIMANT_1_LITIGATION_FRIEND).partyName}`,
    },
    claimant1LRIndividuals: {
      label: 'CLAIMANT 1: Individuals attending for the legal representative',
      selector: '#partyChosen_CLAIMANT_1_LR_INDIVIDUALS',
    },
    claimantsLRIndividuals: {
      label: 'CLAIMANTS: Individuals attending for the legal representative',
      selector: '#partyChosen_CLAIMANT_1_LR_INDIVIDUALS',
    },
    claimant1Witnesses: {
      label: 'CLAIMANT 1: Witnesses',
      selector: '#partyChosen_CLAIMANT_1_WITNESSES',
    },
    claimantsWitnesses: {
      label: 'CLAIMANTS: Witnesses',
      selector: '#partyChosen_CLAIMANT_1_WITNESSES',
    },
    claimant1Experts: {
      label: 'CLAIMANT 1: Experts',
      selector: '#partyChosen_CLAIMANT_1_EXPERTS',
    },
    claimantsExperts: {
      label: 'CLAIMANTS: Experts',
      selector: '#partyChosen_CLAIMANT_1_EXPERTS',
    },
    defendant1: {
      label: (defendantPartyType: ClaimantDefendantPartyType) =>
      `DEFENDANT 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendantPartyType).partyName}`,
      selector: '#partyChosen_DEFENDANT_1',
    },
    defendant1LitigationFriend: {
      label: () => `DEFENDANT 1: Litigation Friend: ${CaseDataHelper.buildLitigationFriendData(partys.DEFENDANT_1_LITIGATION_FRIEND).partyName}`,
      selector: '#partyChosen_DEFENDANT_1_LITIGATION_FRIEND',
    },
    defendant1OrganisationIndividuals: {
      label: 'DEFENDANT 1: Individuals attending for the organisation',
      selector: '#partyChosen_DEFENDANT_1_ORGANISATION_INDIVIDUALS',
    },
    defendant1LRIndividuals: {
      label: 'DEFENDANT 1: Individuals attending for the legal representative',
      selector: '#partyChosen_DEFENDANT_1_LR_INDIVIDUALS',
    },
    defendant1Witnesses: {
      label: 'DEFENDANT 1: Witnesses',
      selector: '#partyChosen_DEFENDANT_1_WITNESSES',
    },
    defendant1Experts: {
      label: 'DEFENDANT 1: Experts',
      selector: '#partyChosen_DEFENDANT_1_EXPERTS',
    },
    defendant2: {
      label: (defendantPartyType: ClaimantDefendantPartyType) =>
      `DEFENDANT 2: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_2, defendantPartyType).partyName}`,
      selector: '#partyChosen_DEFENDANT_2',
    },
    defendant2LitigationFriend: {
      label: () => `DEFENDANT 2: Litigation Friend: ${CaseDataHelper.buildLitigationFriendData(partys.DEFENDANT_2_LITIGATION_FRIEND).partyName}`,
      selector: '#partyChosen_DEFENDANT_2_LITIGATION_FRIEND',
    },
    defendant2OrganisationIndividuals: {
      label: 'DEFENDANT 2: Individuals attending for the organisation',
      selector: '#partyChosen_DEFENDANT_2_ORGANISATION_INDIVIDUALS',
    },
    defendant2LRIndividuals: {
      label: 'DEFENDANT 2: Individuals attending for the legal representative',
      selector: '#partyChosen_DEFENDANT_2_LR_INDIVIDUALS',
    },
    defendant2Witnesses: {
      label: 'DEFENDANT 2: Witnesses',
      selector: '#partyChosen_DEFENDANT_2_WITNESSES',
    },
    defendant2Experts: {
      label: 'DEFENDANT 2: Experts',
      selector: '#partyChosen_DEFENDANT_2_EXPERTS',
    },
    defendantsWitnesses: {
      label: 'DEFENDANTS: Witnesses',
      selector: '#partyChosen_DEFENDANT_2_WITNESSES',
    },
    defendantsExperts: {
      label: 'DEFENDANTS: Experts',
      selector: '#partyChosen_DEFENDANT_2_EXPERTS',
    }
  },
};
