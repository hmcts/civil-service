import partys from "../../../../../constants/users/partys";
import CaseDataHelper from "../../../../../helpers/case-data-helper";
import { ClaimantDefendantPartyType } from "../../../../../models/users/claimant-defendant-party-types";

export const heading = 'Settle this claim';

export const radioButtons = {
  markPaidForAllClaimants: {
    label: 'Does marking this Claim as settled relate to all claimants?',
    yes: {
      label: 'Yes',
      selector: '#markPaidForAllClaimants_Yes',
    },
    no: {
      label: 'No',
      selector: '#markPaidForAllClaimants_No',
    },
  },
  claimantRelatesTo: {
    label: 'Select the claimant this relates to',
    claimant1: {
      label: (claimant1PartyType: ClaimantDefendantPartyType) => CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType).partyName,
    },
    claimant2: {
      label: (claimant2PartyType: ClaimantDefendantPartyType) => CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_2, claimant2PartyType).partyName,
    },
  },
};
