export const subheadings = {
  listYourEvidence: 'List your evidence',
};

export const paragraphs = {
  evidenceInfo:
    'Tell us about any evidence you wish to provide.' +
    ' You do not need to send us any evidence now.' +
    ' If your case goes to a court hearing, and is not settled, you will need to provide evidence.',
};

export const buttons = {
  addNew: { title: 'Add new', selector: "button[class='button write-collection-add-item__top']" },
};

export const dropdowns = {
  evidence: {
    label: 'Please make a selection',
    selector: (evidenceNumber: number) =>
      `#speclistYourEvidenceList_${evidenceNumber - 1}_evidenceType`,
    options: [
      'Contracts and agreements',
      'Expert witness',
      'Letters, emails and other correspondence',
    ],
  },
};

export const inputs = {
  evidence: {
    contractAndAgreements: {
      label:
        'Describe this evidence in more detail (optional). For example, a signed contract. (Optional)',
      selector: (evidenceNumber: number) =>
        `#speclistYourEvidenceList_${evidenceNumber - 1}_contractAndAgreementsEvidence`,
    },
    expertWitness: {
      label:
        "Describe this evidence in more detail (optional). For example, a surveyor's report. (Optional)",
      selector: (evidenceNumber: number) =>
        `#speclistYourEvidenceList_${evidenceNumber - 1}_expertWitnessEvidence`,
    },
    lettersEmailsCorrespondence: {
      label:
        'Describe this evidence in more detail (optional). For example, a letter from the other party. (Optional)',
      selector: (evidenceNumber: number) =>
        `#speclistYourEvidenceList_${evidenceNumber - 1}_lettersEmailsAndOtherCorrespondenceEvidence`,
    },
  },
};
