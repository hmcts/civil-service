import CaseDataHelper from '../../../../../helpers/case-data-helper';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import { Party } from '../../../../../models/users/partys';

export const subheadings = {
    party: (defendantParty: Party, defendantPartyType: ClaimantDefendantPartyType) =>
          `Defendant: ${CaseDataHelper.buildClaimantAndDefendantData(defendantParty, defendantPartyType).partyName}`,
   };
