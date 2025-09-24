export const subheadings = {
  warning: 'Warning',
  judgesRecital: 'Judge’s recital',
  allocation: 'Allocation',
  disputeResolutionHearing: 'Dispute Resolution Hearing',
  legalReprentationForDRH: 'Legal representation for DRH',
  judgePowersAtDRH: 'Judges powers at DRH',
  paymentProtectionInsurance: 'Payment Protection Insurance (PPI)',
  witnessStatements: 'Witness statements',
  uploadOfDocuments: 'Upload of documents',
  addNewDirection: 'Add a new direction (Optional)',
  hearing: 'Hearing',
  welshLanguage: 'Use of the Welsh Language',
  importantNotes: 'Important notes',
};

export const containers = {
  sdoR2SmallClaimsHearing: { selector: '#sdoR2SmallClaimsHearing_sdoR2SmallClaimsHearing' },
};

export const inputs = {
  judgesRecital: {
    selector: '#sdoR2SmallClaimsJudgesRecital_input',
  },
  witnessStatements: {
    statementOfWtinesses: {
      label: 'Statements of witnesses',
      selector: '#sdoR2SmallClaimsWitnessStatements_sdoStatementOfWitness',
    },
    numClaimantWitnesses: {
      label: 'Limit number of witnesses (claimant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictWitness_noOfWitnessClaimant',
    },
    numDefendantWitnesses: {
      label: 'Limit number of witnesses (defendant)',
      hintText: 'For example,4',
      selector:
        '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictWitness_noOfWitnessDefendant',
    },
    partyIsCountedAsWitnessText: {
      selector:
        '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictWitness_partyIsCountedAsWitnessTxt',
    },
    witnessShouldNotMoreThanText: {
      selector:
        '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictPages_witnessShouldNotMoreThanTxt',
    },
    numPages: {
      label: 'Number of pages',
      hintText: 'For example,4',
      selector: '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictPages_noOfPages',
    },
    fontDetails: {
      selector: '#sdoR2SmallClaimsWitnessStatements_sdoR2SmallClaimsRestrictPages_fontDetails',
    },
  },
  uploadOfDocuments: {
    label: 'Upload of documents to be relied upon',
    selector: '#sdoR2SmallClaimsUploadDoc_sdoUploadOfDocumentsTxt',
  },
  hearing: {
    listFrom: {
      label: 'List from',
      selectorKey: 'listFrom',
    },
    dateTo: {
      label: 'Date to',
      selectorKey: 'dateTo',
    },
    bundleOfDocuments: {
      label: 'Bundle of documents',
      selector: '#sdoR2SmallClaimsHearing_sdoR2SmallClaimsBundleOfDocs_physicalBundlePartyTxt',
    },
    hearingNotes: {
      label1: 'Hearing notes',
      label2: 'This is only seen by the Listing Officer (Optional)',
      selector: '#sdoR2SmallClaimsHearing_hearingNotesTxt',
    },
    lengthOfHearing: {
      days: {
        label: 'Days',
        hintText: 'For example,2',
        selector: '#sdoR2SmallClaimsHearing_lengthListOther_trialLengthDays',
      },
      hours: {
        label: 'Hours',
        hintText: 'For example,4',
        selector: '#sdoR2SmallClaimsHearing_lengthListOther_trialLengthHours',
      },
      minutes: {
        label: 'Minutes',
        hintText: 'For example,2',
        selector: '#sdoR2SmallClaimsHearing_lengthListOther_trialLengthMinutes',
      },
    },
  },
  ppiDate: {
    label: 'The Defendant(s) shall by',
    selectorKey: 'ppiDate',
  },
  importantNotes: {
    notes: {
      selector: '#sdoR2SmallClaimsImpNotes_text',
    },
    date: {
      selectorKey: 'date',
      day: {
        label: 'Day',
        selector: '#date-day',
      },
      month: {
        label: 'Month',
        selector: '#date-month',
      },
      year: {
        label: 'Year',
        selector: '#date-year',
      },
    },
  },
  newDirection: {
    label: 'Enter the direction (Optional)',
    selector: '#sdoR2SmallClaimsAddNewDirection_0_directionComment',
  },
};

export const radioButtons = {
  witnessStatements: {
    restrictNumWitnesses: {
      label: 'Restrict number of witnesses',
      yes: {
        label: 'Yes',
        selector: '#sdoR2SmallClaimsWitnessStatements_isRestrictWitness_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2SmallClaimsWitnessStatements_isRestrictWitness_No',
      },
    },
    restrictNumPages: {
      label: 'Restrict number of pages',
      yes: {
        label: 'Yes',
        selector: '#sdoR2SmallClaimsWitnessStatements_isRestrictPages_Yes',
      },
      no: {
        label: 'No',
        selector: '#sdoR2SmallClaimsWitnessStatements_isRestrictPages_No',
      },
    },
  },
  hearing: {
    trialOnOptions: {
      label: 'A hearing will take place on',
      firstOpenDate: {
        label: 'First open date after',
        selector: '#sdoR2SmallClaimsHearing_trialOnOptions-OPEN_DATE',
      },
      hearingWindow: {
        label: 'Hearing window',
        selector: '#sdoR2SmallClaimsHearing_trialOnOptions-HEARING_WINDOW',
      },
    },
    lengthOfHearing: {
      label: 'Length of hearing',
      fifteenMins: {
        label: '15 minutes',
        selector: '#sdoR2SmallClaimsHearing_lengthList-FIFTEEN_MINUTES',
      },
      thirtyMins: {
        label: '30 minutes',
        selector: '#sdoR2SmallClaimsHearing_lengthList-THIRTY_MINUTES',
      },
      oneHour: {
        label: '1 hour',
        selector: '#sdoR2SmallClaimsHearing_lengthList-ONE_HOUR',
      },
      other: {
        label: 'Other',
        selector: '#sdoR2SmallClaimsHearing_lengthList-OTHER',
      },
    },
    hearingLocation: {
      label: 'Hearing location',
      court: {
        selector: 'sdoR2SmallClaimsHearing_hearingCourtLocationList_20262',
      },
      otherLocation: {
        label: 'Other location',
        selector: '#sdoR2SmallClaimsHearing_hearingCourtLocationList_OTHER_LOCATION',
      },
    },
    methodOfHearing: {
      label: 'Method of hearing',
      hintText:
        "If you want to include any extra information or want to request a certain hearing platform, please include this in the 'Hearing notes' section below",
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
    physicalHearingBundle: {
      label: 'Physical hearing bundle',
      no: {
        label: 'No',
        selector: '#sdoR2SmallClaimsHearing_physicalBundleOptions-NO',
      },
      party: {
        label: 'Party',
        selector: '#sdoR2SmallClaimsHearing_physicalBundleOptions-PARTY',
      },
    },
  },
};

export const checkboxes = {
  includePaymentProtectionInsurance: {
    label: 'Include in Order',
    selector: '#sdoR2SmallClaimsPPIToggle-INCLUDE',
  },
  includeWitnessStatements: {
    label: 'Include in Order',
    selector: '#sdoR2SmallClaimsWitnessStatementsToggle-INCLUDE',
  },
  includeUploadOfDocuments: {
    label: 'Include in Order',
    selector: '#sdoR2SmallClaimsUploadDocToggle-INCLUDE',
  },
  includeHearing: {
    label: 'Include in Order',
    selector: '#sdoR2SmallClaimsHearingToggle-INCLUDE',
  },
  includeWelshLanguage: {
    label: 'Include in Order',
    selector: '#sdoR2DrhUseOfWelshIncludeInOrderToggle-INCLUDE',
  },
};

export const buttons = {
  addNewDirection: {
    title: 'Add new',
    selector:
      "div[id='sdoR2SmallClaimsAddNewDirection'] button[class='button write-collection-add-item__top']",
  },
};
