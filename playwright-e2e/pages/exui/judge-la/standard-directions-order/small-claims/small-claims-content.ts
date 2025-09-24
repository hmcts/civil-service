export const heading = 'Order details';

export const subheadings = {
  judgesRecital: "Judge's recital",
  allocation: 'Allocation',
  flightDelay: 'Flight Delay',
  relatedClaims: 'Related claims',
  legalArguments: 'Legal arguments',
  hearingTime: 'Hearing time',
  hearingMethod: 'Hearing Method',
  hearingNotes: 'Hearing notes (Optional)',
  importantNotes: 'Important notes',
  documents: 'Documents',
  witnessStatement: 'Witness Statement',
  creditHire: 'Credit hire',
  roadTrafficAccident: 'Road traffic accident',
  addNewDirection: 'Add a new direction (Optional)',
  welshLanguage: 'Use of the Welsh Language',
  judgementClaimSum: 'Judgment for the claimant for an sum to be decided by the court',
};

export const inputs = {
  judgesRecital: {
    selector: '#smallClaimsJudgesRecital_input',
  },
  flightDelay: {
    relatedClaim: {
      selector: '#smallClaimsFlightDelay_relatedClaimsInput',
    },
    legalArduments: {
      selector: '#smallClaimsFlightDelay_legalDocumentsInput',
    },
  },
  hearingTime: {
    dateFrom: {
      label: 'Date from',
      selectorKey: 'dateFrom-',
    },
    dateTo: {
      label: 'Date from',
      selectorKey: 'dateTo',
    },
    otherHours: {
      label: 'Hour(s)',
      selector: '#smallClaimsHearing_otherHours',
    },
    otherMinutes: {
      label: 'Minute(s)',
      selector: '#smallClaimsHearing_otherMinutes',
    },
    input: {
      selector: '#smallClaimsHearing_input2',
    },
  },
  hearingNotes: {
    hintText: 'This is only seen by the listing officer.',
    selector: '#sdoHearingNotes_input',
  },
  importantNotes: {
    selector: '#smallClaimsNotes_input',
  },
  documents: {
    input1: {
      selector: '#smallClaimsDocuments_input1',
    },
    input2: {
      selector: '#smallClaimsDocuments_input2',
    },
  },
  witnessStatement: {
    statementOfWitnesses: {
      label: 'Statements of witnesses',
      selector: '#sdoR2SmallClaimsWitnessStatementOther_sdoStatementOfWitness',
    },
    numClaimantWitnesses: {
      label: 'Limit number of witnesses (claimant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictWitness_noOfWitnessClaimant',
    },
    numDefendantWitnesses: {
      label: 'Limit number of witnesses (defendant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictWitness_noOfWitnessDefendant',
    },
    partyIsCountedAsWitnessText: {
      selector:
        '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictWitness_partyIsCountedAsWitnessTxt',
    },
    witnessShouldNotMoreThanText: {
      selector:
        '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictPages_witnessShouldNotMoreThanTxt',
    },
    numPages: {
      label: 'Number of pages',
      hintText: 'For example,4',
      selector: '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictPages_noOfPages',
    },
    fontDetails: {
      selector: '#sdoR2SmallClaimsWitnessStatementOther_sdoR2SmallClaimsRestrictPages_fontDetails',
    },
  },
  creditHire: {
    input1: {
      selector: '#smallClaimsCreditHire_input1',
    },
    input2: {
      selector: '#smallClaimsCreditHire_input2',
    },
    input3: {
      selector: '#smallClaimsCreditHire_input3',
    },
    input4: {
      selector: '#smallClaimsCreditHire_input4',
    },
    input5: {
      selector: '#smallClaimsCreditHire_input5',
    },
    input6: {
      selector: '#smallClaimsCreditHire_input6',
    },
    input7: {
      selector: '#smallClaimsCreditHire_input7',
    },
    input8: {
      selector: '#smallClaimsCreditHire_input11',
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
    date4: {
      selectorKey: 'date4',
    },
  },
  roadTrafficAccident: {
    selector: '#smallClaimsRoadTrafficAccident_input',
  },
  newDirection: {
    label: 'Enter the direction (Optional)',
    selector: '#smallClaimsAddNewDirections_0_directionComment',
  },
};

export const checkboxes = {
  flightDelay: {
    label: 'Show/Remove',
    selector: 'label[for="smallClaimsFlightDelayToggle-SHOW"]',
  },
  hearingTime: {
    label: 'Show/Remove',
    selector: 'label[for="smallClaimsHearingToggle-SHOW"]',
  },
  documents: {
    label: 'Show/Remove',
    selector: 'label[for="smallClaimsDocumentsToggle-SHOW"]',
  },
  witnessStatement: {
    label: 'Show/Remove',
    selector: 'label[for="smallClaimsWitnessStatementToggle-SHOW"]',
  },
  welshLanguage: {
    label: 'Show/Remove',
    selector: 'label[for="sdoR2SmallClaimsUseOfWelshToggle-SHOW"]',
  },
};

export const radioButtons = {
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
  hearingTime: {
    label: 'The time estimate is',
    thirtyMins: {
      label: '30 minutes',
      selector: '#smallClaimsHearing_time-THIRTY_MINUTES',
    },
    oneHour: {
      label: '1 hour',
      selector: '#smallClaimsHearing_time-ONE_HOUR',
    },
    oneHourThirtyMins: {
      label: '1.5 hours',
      selector: '#smallClaimsHearing_time-ONE_AND_HALF_HOUR',
    },
    twoHours: {
      label: '2 hours',
      selector: '#smallClaimsHearing_time-TWO_HOURS',
    },
    twohoursThirtyMins: {
      label: '2.5 hours',
      selector: '#smallClaimsHearing_time-TWO_AND_HALF_HOURS',
    },
    other: {
      label: 'Other',
      selector: '#smallClaimsHearing_time-OTHER',
    },
  },
  witnessStatement: {
    restrictNumWitnesses: {
      label: 'Restrict number of witnesses',
      yes: {
        label: 'Yes',
        selector: '#sdoR2SmallClaimsWitnessStatementOther_isRestrictWitness_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2SmallClaimsWitnessStatementOther_isRestrictWitness_No',
      },
    },
    restrictNumPages: {
      label: 'Restrict number of pages',
      yes: {
        label: 'Yes',
        selector: '#sdoR2SmallClaimsWitnessStatementOther_isRestrictPages_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2SmallClaimsWitnessStatementOther_isRestrictPages_No',
      },
    },
  },
};

export const dropdowns = {
  hearingLocation: {
    label: 'This hearing will take place at:',
    selector: '#smallClaimsMethodInPerson',
  },
};

export const buttons = {
  addNewDirection: {
    title: 'Add new',
    selector:
      "div[id='smallClaimsAddNewDirections'] button[class='button write-collection-add-item__top']",
  },
};
