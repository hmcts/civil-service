import CaseDataHelper from '../../../../../helpers/case-data-helper';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import { Party } from '../../../../../models/users/partys';

export const paragraphs = {
    party: (claimantParty: Party, claimantPartyType: ClaimantDefendantPartyType) =>
          `Claimant: ${CaseDataHelper.buildClaimantAndDefendantData(claimantParty, claimantPartyType).partyName}`,
   };
