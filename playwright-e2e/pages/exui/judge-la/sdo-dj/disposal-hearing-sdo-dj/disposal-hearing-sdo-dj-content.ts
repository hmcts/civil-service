export const subheadings = {
  orderHearingDetails: 'Order and hearing details',
  judgesRecital: "Judge's recital",
  judgementForClaimant:
    'There is judgment for the claimant for an amount to be decided by the court',
  disclosureOfDocuments: 'Disclosure of documents',
  witnessOfFact: 'Witnesses of Fact',
  medicalEvidence: 'Medical evidence',
  questionsToExperts: 'Questions to experts',
  scheduleOfLoss: 'Schedules or counter-schedules of loss',
  hearingTime: 'Hearing time',
  hearingMethod: 'Hearing Method',
  claimSettling: 'Claim settling',
  costs: 'Costs',
  hearingNotes: 'Hearing notes',
  welshLanguage: 'Use of the Welsh language',
  importantNotes: 'Important notes',
  newDirection: 'Add a new direction',
};

export const containers = {
  disclosureOfDocuments: { selector: '#disposalHearingDisclosureOfDocumentsDJ' },
  witnessOfFact: { selector: '#disposalHearingWitnessOfFactDJ_disposalHearingWitnessOfFactDJ' },
  medicalEvidence: {
    selector: '#disposalHearingMedicalEvidenceDJ_disposalHearingMedicalEvidenceDJ',
  },
  questionsToExperts: {
    selector: '#disposalHearingQuestionsToExpertsDJ_disposalHearingQuestionsToExpertsDJ',
  },
  schedulesOfLoss: {
    selector: '#disposalHearingSchedulesOfLossDJ_disposalHearingSchedulesOfLossDJ',
  },
};

export const inputs = {
  judgesRecital: {
    selector: '#disposalHearingJudgesRecitalDJ_input',
  },
  disclosureOfDocuments: {
    input1: {
      selector: '#disposalHearingDisclosureOfDocumentsDJ_input',
    },
    date1: {
      selectorKey: 'date1',
    },
  },
  witnessOfFact: {
    input1: {
      selector: '#disposalHearingWitnessOfFactDJ_input1',
    },
    input2: {
      selector: '#disposalHearingWitnessOfFactDJ_input2',
    },
    input3: {
      selector: '#disposalHearingWitnessOfFactDJ_input3',
    },
    input4: {
      selector: '#disposalHearingWitnessOfFactDJ_input4',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
  },
  medicalEvidence: {
    input: {
      selector: '#disposalHearingMedicalEvidenceDJ_input1',
    },
    date: {
      selectorKey: 'date1',
    },
  },
  questionsToExperts: {
    date: {
      selectorKey: 'date',
    },
  },
  scheduleOfLoss: {
    input1: {
      selector: '#disposalHearingSchedulesOfLossDJ_input1',
    },
    input2: {
      selector: '#disposalHearingSchedulesOfLossDJ_inputText2',
    },
    input3: {
      selector: '#disposalHearingSchedulesOfLossDJ_inputText3',
    },
    input4: {
      selector: '#disposalHearingSchedulesOfLossDJ_inputText4',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
    date3: {
      selectorKey: 'date3',
    },
  },
  hearingTime: {
    input: {
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_input',
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
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_otherHours',
    },
    otherMinutes: {
      label: 'Minutes',
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_otherMinutes',
    },
  },
  newDirection: {
    label: 'Enter the direction (Optional)',
    selector: '#disposalHearingAddNewDirectionsDJ_0_directionComment',
  },
  hearingNotes: {
    label: 'This is only seen by the listing officer (Optional)',
    selector: '#disposalHearingHearingNotesDJ_input',
  },
  importantNotes: {
    selector: '#disposalHearingOrderMadeWithoutHearingDJ_input',
  },
};

export const radioButtons = {
  hearingTime: {
    label: 'The time estimate is',
    thirtyMins: {
      label: '30 minutes',
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_time-THIRTY_MINUTES',
    },
    fifteenMins: {
      label: '15 minutes',
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_time-FIFTEEN_MINUTES',
    },
    other: {
      label: 'Other',
      selector: '#disposalHearingFinalDisposalHearingTimeDJ_time-OTHER',
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
    selector: '#disposalHearingDisclosureOfDocumentsDJToggle-SHOW',
  },
  witnessesOfFact: {
    label: 'Show/Remove',
    selector: '#disposalHearingWitnessOfFactDJToggle-SHOW',
  },
  medicalEvidence: {
    label: 'Show/Remove',
    selector: '#disposalHearingMedicalEvidenceDJToggle-SHOW',
  },
  questionsToExperts: {
    label: 'Show/Remove',
    selector: '#disposalHearingQuestionsToExpertsDJToggle-SHOW',
  },
  scheduleOfLoss: {
    label: 'Show/Remove',
    selector: '#disposalHearingSchedulesOfLossDJToggle-SHOW',
  },
  hearingTime: {
    label: 'Show/Remove',
    selector: '#disposalHearingFinalDisposalHearingToggle-SHOW',
  },
  claimSettling: {
    label: 'Show/Remove',
    selector: '#disposalHearingClaimSettlingDJToggle-SHOW',
  },
  costs: {
    label: 'Show/Remove',
    selector: '#disposalHearingCostsDJToggle-SHOW',
  },
  welshLanguage: {
    label: 'Show/Remove',
    selector: '#sdoR2DisposalHearingUseOfWelshDJToggle-SHOW',
  },
};

export const dropdowns = {
  hearingMethod: {
    label: 'Select hearing location',
    selector: '#disposalHearingMethodInPersonDJ',
  },
};

export const buttons = {
  addNewDirection: {
    title: 'Add new',
    selector:
      "div[id='disposalHearingAddNewDirectionsDJ'] button[class='button write-collection-add-item__top']",
  },
};
