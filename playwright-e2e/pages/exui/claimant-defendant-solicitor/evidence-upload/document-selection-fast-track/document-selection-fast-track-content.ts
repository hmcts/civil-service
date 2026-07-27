import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';

export const heading = 'Upload Your Documents';

export const subheadings = {
  disclosure: 'Disclosure',
  witnessEvidence: 'Witness evidence',
  expertEvidence: 'Expert evidence',
  trialDocuments: 'Trial Documents',
};

const res = (party: Party) => (party === partys.CLAIMANT_1 ? '' : 'Res');

export const checkboxes = {
  disclosureList: {
    label: 'Disclosure list',
    selector: (party: Party) => `#disclosureSelectionEvidence${res(party)}-DISCLOSURE_LIST`,
  },
  documentsForDisclosure: {
    label: 'Documents for disclosure',
    selector: (party: Party) =>
      `#disclosureSelectionEvidence${res(party)}-DOCUMENTS_FOR_DISCLOSURE`,
  },
  witnessStatement: {
    label: 'Witness statement',
    selector: (party: Party) => `#witnessSelectionEvidence${res(party)}-WITNESS_STATEMENT`,
  },
  witnessSummary: {
    label: 'Witness summary',
    selector: (party: Party) => `#witnessSelectionEvidence${res(party)}-WITNESS_SUMMARY`,
  },
  noticeOfIntention: {
    label: 'Notice of the intention to rely on hearsay evidence',
    selector: (party: Party) => `#witnessSelectionEvidence${res(party)}-NOTICE_OF_INTENTION`,
  },
  documentsReferred: {
    label: 'Documents referred to in the statement',
    selector: (party: Party) => `#witnessSelectionEvidence${res(party)}-DOCUMENTS_REFERRED`,
  },
  expertsReport: {
    label: "Expert's report",
    selector: (party: Party) => `#expertSelectionEvidence${res(party)}-EXPERT_REPORT`,
  },
  jointStatement: {
    label: 'Joint Statement of Experts / Single Joint Expert Report',
    selector: (party: Party) => `#expertSelectionEvidence${res(party)}-JOINT_STATEMENT`,
  },
  questionsForExperts: {
    label: 'Questions asked of other party expert',
    selector: (party: Party) => `#expertSelectionEvidence${res(party)}-QUESTIONS_FOR_EXPERTS`,
  },
  answersForExperts: {
    label: 'Answer to questions asked',
    selector: (party: Party) => `#expertSelectionEvidence${res(party)}-ANSWERS_FOR_EXPERTS`,
  },
  caseSummary: {
    label: 'Case Summary',
    selector: (party: Party) => `#trialSelectionEvidence${res(party)}-CASE_SUMMARY`,
  },
  skeletonArgument: {
    label: 'Skeleton argument',
    selector: (party: Party) => `#trialSelectionEvidence${res(party)}-SKELETON_ARGUMENT`,
  },
  authorities: {
    label: 'Authorities',
    selector: (party: Party) => `#trialSelectionEvidence${res(party)}-AUTHORITIES`,
  },
  costs: {
    label: 'Costs',
    selector: (party: Party) => `#trialSelectionEvidence${res(party)}-COSTS`,
  },
  documentary: {
    label: 'Documentary evidence for trial',
    selector: (party: Party) => `#trialSelectionEvidence${res(party)}-DOCUMENTARY`,
  },
};
