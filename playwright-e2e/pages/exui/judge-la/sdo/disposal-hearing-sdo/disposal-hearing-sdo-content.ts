export const subheadings = {
  orderHearingDetails: 'Order and hearing details',
  judgesRecital: "Judge's recital",
  judgementForClaimant:
    'There is a judgment for the claimant for an amount to be decided by the court',
  disclosureOfDocuments: 'Disclosure of documents',
  witnessOfFact: 'Witnesses of Fact',
  medicalEvidence: 'Medical evidence',
  questionsToExperts: 'Questions to experts',
  scheduleOfLoss: 'Schedules or counter-schedules of loss',
  hearingTime: 'Hearing time',
  hearingMethod: 'Hearing Method',
  claimSettling: 'Claim Settling',
  costs: 'Costs',
  newDirection: 'Add a new direction (Optional)',
  hearingNotes: 'Hearing notes',
  welshLanguage: 'Use of the Welsh Language',
  importantNotes: 'Important notes',
};

export const containers = {
  disclosureOfDocuments: { selector: '#disposalHearingDisclosureOfDocuments' },
  witnessOfFact: { selector: '#disposalHearingWitnessOfFact_disposalHearingWitnessOfFact' },
  medicalEvidence: { selector: '#disposalHearingMedicalEvidence_disposalHearingMedicalEvidence' },
  questionsToExperts: {
    selector: '#disposalHearingQuestionsToExperts_disposalHearingQuestionsToExperts',
  },
  schedulesOfLoss: {
    selector: '#disposalHearingSchedulesOfLoss_disposalHearingSchedulesOfLoss',
  },
};

export const inputs = {
  judgesRecital: {
    selector: '#disposalHearingJudgesRecital_input',
  },
  disclosureOfDocuments: {
    input1: {
      selector: '#disposalHearingDisclosureOfDocuments_input1',
    },
    date1: {
      selectorKey: 'date1',
    },
  },
  witnessOfFact: {
    input3: {
      selector: '#disposalHearingWitnessOfFact_input3',
    },
    input4: {
      selector: '#disposalHearingWitnessOfFact_input4',
    },
    input5: {
      selector: '#disposalHearingWitnessOfFact_input5',
    },
    input6: {
      selector: '#disposalHearingWitnessOfFact_input6',
    },
    date2: {
      selectorKey: 'date2',
    },
    date3: {
      selectorKey: 'date3',
    },
  },
  medicalEvidence: {
    input: {
      selector: '#disposalHearingMedicalEvidence_input',
    },
    date: {
      selectorKey: 'date',
    },
  },
  questionsToExperts: {
    date: {
      selectorKey: 'date',
    },
  },
  scheduleOfLoss: {
    input2: {
      selector: '#disposalHearingSchedulesOfLoss_input2',
    },
    input3: {
      selector: '#disposalHearingSchedulesOfLoss_input3',
    },
    input4: {
      selector: '#disposalHearingSchedulesOfLoss_input4',
    },
    date2: {
      selectorKey: 'date2',
    },
    date3: {
      selectorKey: 'date3',
    },
    date4: {
      selectorKey: 'date4',
    },
  },
  hearingTime: {
    input: {
      selector: '#disposalHearingHearingTime_input',
    },
    dateFrom: {
      label: 'Date from',
      selectorKey: 'dateFrom',
    },
    dateTo: {
      label: 'Date to',
      selectorKey: 'dateTo',
    },
    otherHours: {
      label: 'Hours',
      selector: '#disposalHearingHearingTime_otherHours',
    },
    otherMinutes: {
      label: 'Minutes',
      selector: '#disposalHearingHearingTime_otherMinutes',
    },
  },
  newDirection: {
    label: 'Enter the direction (Optional)',
    selector: '#disposalHearingAddNewDirections_0_directionComment',
  },
  hearingNotes: {
    label: 'This is only seen by the listing officer (Optional)',
    selector: '#disposalHearingHearingNotes',
  },
  importantNotes: {
    selector: '#disposalOrderWithoutHearing_input',
  },
};

export const radioButtons = {
  hearingTime: {
    label: 'The time estimate is',
    thirtyMins: {
      label: '30 minutes',
      selector: '#disposalHearingHearingTime_time-THIRTY_MINUTES',
    },
    fifteenMins: {
      label: '15 minutes',
      selector: '#disposalHearingHearingTime_time-FIFTEEN_MINUTES',
    },
    other: {
      label: 'Other',
      selector: '#disposalHearingHearingTime_time-OTHER',
    },
  },
  hearingMethod: {
    label: 'Select an option below',
    inPerson: {
      label: 'In Person',
    },
    telephone: {
      label: 'Telephone',
    },
    video: {
      label: 'Video',
    },
  },
};

export const checkboxes = {
  disclosureOfDocuments: {
    label: 'Show/Remove',
    selector: '#disposalHearingDisclosureOfDocumentsToggle-SHOW',
  },
  witnessesOfFact: {
    label: 'Show/Remove',
    selector: '#disposalHearingWitnessOfFactToggle-SHOW',
  },
  medicalEvidence: {
    label: 'Show/Remove',
    selector: '#disposalHearingMedicalEvidenceToggle-SHOW',
  },
  questionsToExperts: {
    label: 'Show/Remove',
    selector: '#disposalHearingQuestionsToExpertsToggle-SHOW',
  },
  scheduleOfLoss: {
    label: 'Show/Remove',
    selector: '#disposalHearingSchedulesOfLossToggle-SHOW',
  },
  hearingTime: {
    label: 'Show/Remove',
    selector: '#disposalHearingFinalDisposalHearingToggle-SHOW',
  },
  claimSettling: {
    label: 'Show/Remove',
    selector: '#disposalHearingClaimSettlingToggle-SHOW',
  },
  costs: {
    label: 'Show/Remove',
    selector: '#disposalHearingCostsToggle-SHOW',
  },
  welshLanguage: {
    label: 'Show/Remove',
    selector: '#sdoR2DisposalHearingUseOfWelshToggle-SHOW',
  },
};

export const dropdowns = {
  hearingMethod: {
    label: 'Select hearing location',
    selector: '#disposalHearingMethodInPerson',
  },
};

export const buttons = {
  addNewDirection: {
    title: 'Add new',
    selector:
      "div[id='disposalHearingAddNewDirections'] button[class='button write-collection-add-item__top']",
  },
};
