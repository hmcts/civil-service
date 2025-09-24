export const heading = 'Order details';

export const subheadings = {
  judgesRecital: "Judge's recital",
  allocation: 'Allocation',
  altDisputeResolution: 'Alternative dispute resolution',
  variationOfDirections: 'Variation of directions',
  settlement: 'Settlement',
  disclosureOfDocuments: 'Disclosure of documents',
  witnessesOfFact: 'Witnesses of Fact',
  schedulesOfLoss: 'Schedules of loss',
  hearingTime: 'Hearing time',
  hearingMethod: 'Hearing Method',
  buildingDispute: 'Building dispute',
  clinicalNegligence: 'Clinical negligence',
  creditHire: 'Credit hire',
  employersLiability: "Employer's liability",
  housingDisrepair: 'Housing Disrepair',
  expertEvidence: 'Expert Evidence',
  roadTrafficAccident: 'Road traffic accident',
  newDirection: 'Add a new direction (Optional)',
  hearingNotes: 'Hearing notes',
  welshLanguage: 'Use of the Welsh Language',
  importantNotes: 'Important notes',
  judgementClaimSum:
    'There is a judgment for the claimant for an amount to be decided by the court',
};

export const containers = {
  disclosureDocuments: {
    selector: '#fastTrackDisclosureOfDocuments_fastTrackDisclosureOfDocuments',
  },
  schedulesOfLoss: {
    selector: '#fastTrackSchedulesOfLoss_fastTrackSchedulesOfLoss',
  },
  buildingDispute: {
    selector: '#fastTrackBuildingDispute_fastTrackBuildingDispute',
  },
  creditHire: {
    selector: '#sdoR2FastTrackCreditHire_sdoR2FastTrackCreditHire',
  },
  housingDisrepair: {
    selector: '#fastTrackHousingDisrepair_fastTrackHousingDisrepair',
  },
  personalInjury: {
    selector: '#fastTrackPersonalInjury_fastTrackPersonalInjury',
  },
};

export const inputs = {
  judgesRecital: {
    selector: '#fastTrackJudgesRecital_input',
  },
  allocationReasons: {
    label: 'because (Optional)',
    selector: '#fastTrackAllocation_reasons',
  },
  disclosureOfDocuments: {
    input1: {
      selector: '#fastTrackDisclosureOfDocuments_input1',
    },
    input2: {
      selector: '#fastTrackDisclosureOfDocuments_input2',
    },
    input3: {
      selector: '#fastTrackDisclosureOfDocuments_input3',
    },
    input4: {
      selector: '#fastTrackDisclosureOfDocuments_input4',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
    date3: {
      selectorKey: 'date2',
    },
  },
  witnessesOfFact: {
    statementsOfWitnesses: {
      label: 'Statements of witnesses',
      selector: '#sdoR2FastTrackWitnessOfFact_sdoStatementOfWitness',
    },
    deadline: {
      label: 'Deadline',
      selector: '#sdoR2FastTrackWitnessOfFact_sdoWitnessDeadline',
    },
    deadlineDate: {
      selectorKey: 'sdoWitnessDeadlineDate',
    },
    deadlineText: {
      selector: '#sdoR2FastTrackWitnessOfFact_sdoWitnessDeadlineText',
    },
    numClaimantWitnesses: {
      label: 'Limit number of witnesses (claimant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2FastTrackWitnessOfFact_sdoR2RestrictWitness_restrictNoOfWitnessDetails_noOfWitnessClaimant',
    },
    numDefendantWitnesses: {
      label: 'Limit number of witnesses (defendant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2FastTrackWitnessOfFact_sdoR2RestrictWitness_restrictNoOfWitnessDetails_noOfWitnessDefendant',
    },
    partyIsCountedAsWitnessText: {
      selector:
        '#sdoR2FastTrackWitnessOfFact_sdoR2RestrictWitness_restrictNoOfWitnessDetails_partyIsCountedAsWitnessTxt',
    },
    witnessShouldNotMoreThanText: {
      selector:
        '#sdoR2FastTrackWitnessOfFact_sdoRestrictPages_restrictNoOfPagesDetails_witnessShouldNotMoreThanTxt',
    },
    numPages: {
      label: 'Number of pages',
      hintText: 'For example,4',
      selector: '#sdoR2FastTrackWitnessOfFact_sdoRestrictPages_restrictNoOfPagesDetails_noOfPages',
    },
    fontDetails: {
      selector:
        '#sdoR2FastTrackWitnessOfFact_sdoRestrictPages_restrictNoOfPagesDetails_fontDetails',
    },
  },
  scheduleOfLoss: {
    input1: {
      selector: '#fastTrackSchedulesOfLoss_input1',
    },
    input2: {
      selector: '#fastTrackSchedulesOfLoss_input2',
    },
    input3: {
      selector: '#fastTrackSchedulesOfLoss_input3',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
  },
  hearingTime: {
    dateFrom: {
      label: 'Date from',
      selectorKey: 'dateFrom',
    },
    dateTo: {
      label: 'Date to (Optional)',
      selectorKey: 'dateTo',
    },
    helpText1: {
      selector: '#fastTrackHearingTime_helpText1',
    },
    otherHours: {
      label: 'Hours',
      selector: '#fastTrackHearingTime_otherHours',
    },
    otherMinutes: {
      label: 'Minutes',
      selector: '#fastTrackHearingTime_otherMinutes',
    },
  },
  buildingDispute: {
    input1: {
      hintText: 'Scott schedule',
      selector: '#fastTrackBuildingDispute_input1',
    },
    input2: {
      selector: '#fastTrackBuildingDispute_input2',
    },
    input3: {
      selector: '#fastTrackBuildingDispute_input3',
    },
    input4: {
      selector: '#fastTrackBuildingDispute_input4',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
  },
  clinicalNegliegence: {
    input1: {
      hintText: 'Retention of documents',
      selector: '#fastTrackClinicalNegligence_input1',
    },
    input2: {
      selector: '#fastTrackClinicalNegligence_input2',
    },
    input3: {
      selector: '#fastTrackClinicalNegligence_input3',
    },
    input4: {
      selector: '#fastTrackClinicalNegligence_input4',
    },
  },
  creditHire: {
    input1: {
      selector: '#sdoR2FastTrackCreditHire_input1',
    },
    input2: {
      selector: '#sdoR2FastTrackCreditHire_sdoR2FastTrackCreditHireDetails_input2',
    },
    input3: {
      selector: '#sdoR2FastTrackCreditHire_sdoR2FastTrackCreditHireDetails_input3',
    },
    input4: {
      selector: '#sdoR2FastTrackCreditHire_sdoR2FastTrackCreditHireDetails_input4',
    },
    input5: {
      selector: '#sdoR2FastTrackCreditHire_input5',
    },
    input6: {
      selector: '#sdoR2FastTrackCreditHire_input6',
    },
    input7: {
      selector: '#sdoR2FastTrackCreditHire_input7',
    },
    input8: {
      selector: '#sdoR2FastTrackCreditHire_input8',
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
  housingDisrepair: {
    input1: {
      hintText: 'Scott schedule',
      selector: '#fastTrackHousingDisrepair_input1',
    },
    input2: {
      selector: '#fastTrackHousingDisrepair_input2',
    },
    input3: {
      selector: '#fastTrackHousingDisrepair_input3',
    },
    input4: {
      selector: '#fastTrackHousingDisrepair_input4',
    },
    date1: {
      selectorKey: 'date1',
    },
    date2: {
      selectorKey: 'date2',
    },
  },
  expertEvidence: {
    input1: {
      selector: '#fastTrackPersonalInjury_input1',
    },
    input2: {
      selector: '#fastTrackPersonalInjury_input2',
    },
    input3: {
      selector: '#fastTrackPersonalInjury_input3',
    },
    input4: {
      selector: '#fastTrackPersonalInjury_input4',
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
    input: {
      selector: '#fastTrackRoadTrafficAccident_input',
    },
    date: {
      selectorKey: 'date',
    },
  },
  hearingNotes: {
    label: 'This is only seen by the listing officer (Optional)',
    selector: '#fastTrackHearingNotes_input',
  },
  importantNotes: {
    selector: '#fastTrackOrderWithoutJudgement_input',
  },
  newDirection: {
    label: 'Enter the direction (Optional',
    selector: '#fastTrackAddNewDirections_0_directionComment',
  },
};

export const radioButtons = {
  allocation: {
    assignComplexityBand: {
      label: 'Do you assign a complexity band to the case?',
      yes: {
        label: 'Yes',
        selector: '#fastTrackAllocation_assignComplexityBand_Yes',
      },
      no: {
        label: 'No',
        selector: '#fastTrackAllocation_assignComplexityBand_No',
      },
    },
    allocationComplexity: {
      label: 'and the court assigns the claim to complexity',
      band1: {
        label: 'band 1: road traffic accident without personal injury; debt claims',
        selector: '#fastTrackAllocation_band-BAND_1',
      },
      band2: {
        label:
          'band 2: road traffic accident with personal injury covered by protocol; personal injury; package travel claims',
        selector: '#fastTrackAllocation_band-BAND_2',
      },
      band3: {
        label:
          'band 3: road traffic accident with personal injury but not covered by protocol; employer liability (accident): public liability (personal injury); housing disrepair; other money claims',
        selector: '#fastTrackAllocation_band-BAND_3',
      },
      band4: {
        label:
          'band 4: employer liability (disease, but not noise induced hearing loss) complex housing disrepair, property/building disputes; professional negligence; complex claims',
        selector: '#fastTrackAllocation_band-BAND_4',
      },
    },
  },
  witnessesOfFact: {
    restrictNumWitnesses: {
      label: 'Restrict number of witnesses',
      yes: {
        label: 'Yes',
        selector: '#sdoR2FastTrackWitnessOfFact_sdoR2RestrictWitness_isRestrictWitness_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2FastTrackWitnessOfFact_sdoR2RestrictWitness_isRestrictWitness_No',
      },
    },
    restrictNumPages: {
      label: 'Restrict number of pages',
      yes: {
        label: 'Yes',
        selector: '#sdoR2FastTrackWitnessOfFact_sdoRestrictPages_isRestrictPages_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2FastTrackWitnessOfFact_sdoRestrictPages_isRestrictPages_No',
      },
    },
  },
  hearingTime: {
    label: 'The time estimate is',
    oneHour: {
      label: '1 hour',
      selector: '#fastTrackHearingTime_hearingDuration-ONE_HOUR',
    },
    oneHourThirtyMins: {
      label: '1.5 hours',
      selector: '#fastTrackHearingTime_hearingDuration-ONE_AND_HALF_HOUR',
    },
    twoHours: {
      label: '2 hours',
      selector: '#fastTrackHearingTime_hearingDuration-TWO_HOURS',
    },
    threeHours: {
      label: '3 hours',
      selector: '#fastTrackHearingTime_hearingDuration-THREE_HOURS',
    },
    fourHours: {
      label: '4 hours',
      selector: '#fastTrackHearingTime_hearingDuration-FOUR_HOURS',
    },
    fiveHours: {
      label: '5 hours',
      selector: '#fastTrackHearingTime_hearingDuration-FIVE_HOURS',
    },
    other: {
      label: 'Other',
      selector: '#fastTrackHearingTime_hearingDuration-OTHER',
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
  includeAltDisputeResolution: {
    label: 'Show/Remove',
    selector: '#fastTrackAltDisputeResolutionToggle-SHOW',
  },
  includeVariationOfDirections: {
    label: 'Show/Remove',
    selector: '#fastTrackVariationOfDirectionsToggle-SHOW',
  },
  includeSettlement: {
    label: 'Show/Remove',
    selector: '#fastTrackSettlementToggle-SHOW',
  },
  includeWitnessesOfFact: {
    label: 'Show/Remove',
    selector: '#fastTrackWitnessOfFactToggle-SHOW',
  },
  includeScheduleOfLoss: {
    label: 'Show/Remove',
    selector: '#fastTrackSchedulesOfLossToggle-SHOW',
  },
  includeHearingTime: {
    label: 'Show/Remove',
    selector: '#fastTrackTrialToggle-SHOW',
  },
  includeHearingTimeDateTo: {
    label: 'Show/Remove',
    selector: '#fastTrackHearingTime_dateToToggle-SHOW',
  },
  includeCreditHireDetails: {
    label: 'Add/Remove',
    selector: '#sdoR2FastTrackCreditHire_detailsShowToggle-ADD',
  },
  includeWelshLanguage: {
    label: 'Add/Remove',
    selector: '#sdoR2FastTrackUseOfWelshToggle-SHOW',
  },
};

export const dropdowns = {
  hearingMethod: {
    label: 'This hearing will take place at:',
    selector: '#fastTrackMethodInPerson',
  },
};

export const buttons = {
  addNewDirection: {
    title: 'Add new',
    selector:
      "div[id='fastTrackAddNewDirections'] button[class='button write-collection-add-item__top']",
  },
};
