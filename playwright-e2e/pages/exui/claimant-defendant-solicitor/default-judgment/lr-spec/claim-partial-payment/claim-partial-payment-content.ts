import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types';
import { Party } from '../../../../../../models/partys';

export const subheadings = {
  hasPaid: (defendantParty: Party, defendantPartyType: ClaimantDefendantPartyType) =>
    `Has ${CaseDataHelper.buildClaimantAndDefendantData(defendantParty, defendantPartyType).partyName} paid some of the amount owed?`,
  hasPaid1v2: 'Have the defendants paid some of the amount owed?',
};

export const radioButtons = {
  partialPayment: {
    yes: {
      label: 'Yes',
      selector: '#partialPayment_Yes',
    },
    no: {
      label: 'No',
      selector: '#partialPayment_No',
    },
  },
};

export const inputs = {
  amount: {
    label: 'Amount already paid',
    selector: '#partialPaymentAmount',
  },
};
