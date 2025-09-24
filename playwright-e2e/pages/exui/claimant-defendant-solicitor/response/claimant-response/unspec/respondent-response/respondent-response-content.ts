import StringHelper from '../../../../../../../helpers/string-helper';
import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  docUrl: 'Response document',
};

export const paragraphs = {
  rejectAll: 'Reject all of the claim',
};

export const radioButtons = {
  proceedWithClaim: {
    label: 'Do you want to proceed with the claim',
    label1v2: `Do you want to proceed with the claim against`,
    label2v1: (claimantParty: Party) =>
      `Does Claimant ${claimantParty.number} want to proceed with the claim against`,
    yes: {
      label: 'Yes',
      selector: '#applicant1ProceedWithClaim_Yes',
      selector1v2: (defendantParty: Party) =>
        `#applicant1ProceedWithClaimAgainst${StringHelper.capitalise(defendantParty.oldKey)}MultiParty1v2_Yes`,
      selector2v1: (claimantParty: Party) =>
        `#${claimantParty.oldKey}ProceedWithClaimMultiParty2v1_Yes`,
    },
    no: {
      label: 'No',
      selector: '#applicant1ProceedWithClaim_No',
      selector1v2: (defendantParty: Party) =>
        `#applicant1ProceedWithClaimAgainst${StringHelper.capitalise(defendantParty.oldKey)}MultiParty1v2_No`,
      selector2v1: (claimantParty: Party) =>
        `#${claimantParty.oldKey}ProceedWithClaimMultiParty2v1_No`,
    },
  },
};
