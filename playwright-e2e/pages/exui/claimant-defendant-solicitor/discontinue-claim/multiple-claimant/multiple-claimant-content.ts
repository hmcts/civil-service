import partys from "../../../../../constants/users/partys";
import CaseDataHelper from "../../../../../helpers/case-data-helper";
import { ClaimantDefendantPartyType } from "../../../../../models/users/claimant-defendant-party-types";

export const headings = {
  discontinueThisClaim: 'Discontinue this claim',
  whoIsDiscontinuing: 'Who is discontinuing?',
};

export const radioButtons = {
  claimantWhoIsDiscontinuing: {
    label: 'Which claimants are discontinuing?',
    claimant1: {
      label: (claimantDefendantPartyType: ClaimantDefendantPartyType) =>
        CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimantDefendantPartyType).partyName,
    },
    claimant2: {
      label: (claimantDefendantPartyType: ClaimantDefendantPartyType) =>
        CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_2, claimantDefendantPartyType).partyName,
    },
    both: {
      label: 'Both',
    },
  },
};
