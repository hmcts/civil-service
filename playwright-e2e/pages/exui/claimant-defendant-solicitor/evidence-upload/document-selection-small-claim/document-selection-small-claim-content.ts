import partys from "../../../../../constants/users/partys";
import { Party } from "../../../../../models/users/partys";

export const heading = 'Select the type of document you would like to upload';

export const subheadings = {
  witnessEvidence: 'Witness evidence',
  expertEvidence: 'Expert evidence',
  trialDocuments: 'Trial Documents',
};

export const checkboxes = {
  witnessStatement: {
    label: 'Witness statement',
    selector: (claimantDefendantParty: Party) => `#witnessSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res' }-WITNESS_STATEMENT`
  },
  witnessSummary: {
    label: 'Witness summary',
    selector: (claimantDefendantParty: Party) => `#witnessSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-WITNESS_SUMMARY`
  },
  documentaryEvidence: {
    label: 'Documentary evidence',
    selector: (claimantDefendantParty: Party) => `#witnessSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-DOCUMENTS_REFERRED`
  },
  expertsReport: {
    label: "Expert's report",
    selector: (claimantDefendantParty: Party) => `#expertSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-EXPERT_REPORT`
  },
  jointStatementOfExperts: {
    label: 'Joint Statement of Experts / Single Joint Expert Report',
    selector: (claimantDefendantParty: Party) => `#expertSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-JOINT_STATEMENT`
  },
  authorities: {
    label: 'Authorities',
    selector: (claimantDefendantParty: Party) => `#trialSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-AUTHORITIES`
  },
  costs: {
    label: 'Costs',
    selector: (claimantDefendantParty: Party) => `#trialSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-COSTS`
  },
  documentaryEvidenceForTrial: {
    label: 'Documentary evidence for trial',
    selector: (claimantDefendantParty: Party) => `#trialSelectionEvidenceSmallClaim${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}-DOCUMENTARY`
  },
};
