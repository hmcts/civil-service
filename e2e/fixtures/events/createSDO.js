const {date, element} = require('../../api/dataHelper');

const calculatedClaimsTrackWOSum = {
  ClaimsTrack: {
    fastTrackJudgementDeductionValue: (data) => typeof data.value === 'string',
    smallClaimsJudgementDeductionValue:  (data) => typeof data.value === 'string',
    disposalHearingJudgementDeductionValue:  (data) => typeof data.value === 'string',
    smallClaimsHearing: (data) => {
      return typeof data.input1 === 'string' && typeof data.input2 === 'string';
    },
    disposalHearingDisclosureOfDocumentsToggle: (data) => Array.isArray(data),
    disposalHearingFinalDisposalHearing: (data) => {
      return typeof data.input === 'string' && data.date.match(/\d{4}-\d{2}-\d{2}/);
    },
    fastTrackBuildingDispute: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    fastTrackDisclosureOfDocuments: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    fastTrackSettlementToggle: (data) => Array.isArray(data),
    disposalHearingWitnessOfFactToggle: (data) => Array.isArray(data),
    fastTrackSchedulesOfLoss: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string';
    },
    disposalHearingNotes: (data) => {
      return typeof data.input === 'string';
    },
    smallClaimsMethod: (data) => typeof data === 'string',
    fastTrackWitnessOfFactToggle: (data) => Array.isArray(data),
    smallClaimsWitnessStatement: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.text === 'string'
        && typeof data.input4 === 'string';
    },
    disposalHearingFinalDisposalHearingToggle: (data) => Array.isArray(data),
    fastTrackMethodInPerson: (data) => {
      return data.value.code && data.value.label && Array.isArray(data.list_items);
    },
    fastTrackDisclosureOfDocumentsToggle: (data) => Array.isArray(data),
    disposalHearingMedicalEvidenceToggle: (data) => Array.isArray(data),
    disposalHearingSchedulesOfLossToggle: (data) => Array.isArray(data),
    disposalHearingSchedulesOfLoss: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    fastTrackAltDisputeResolutionToggle: (data) => Array.isArray(data),
    fastTrackPersonalInjury: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    disposalHearingBundleToggle: (data) => Array.isArray(data),
    smallClaimsCreditHire: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string'
        && typeof data.input5 === 'string'
        && typeof data.input6 === 'string'
        && typeof data.input7 === 'string';
    },
    disposalHearingCostsToggle: (data) => Array.isArray(data),
    smallClaimsWitnessStatementToggle: (data) => Array.isArray(data),
    smallClaimsHearingToggle: (data) => Array.isArray(data),
    fastTrackMethodToggle: (data) => Array.isArray(data),
    disposalHearingBundle: (data) => {
      return typeof data.input === 'string';
    },
    fastTrackWitnessOfFact: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input4 === 'string'
        && typeof data.input5 === 'string'
        && typeof data.input7 === 'string'
        && typeof data.input8 === 'string'
        && typeof data.input9 === 'string';
    },
    fastTrackClinicalNegligence: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    fastTrackTrialToggle: (data) => Array.isArray(data),
    fastTrackTrialBundleToggle: (data) => Array.isArray(data),
    fastTrackNotes: (data) => {
      return typeof data.input === 'string';
    },
    disposalHearingMethodToggle: (data) => Array.isArray(data),
    disposalHearingMedicalEvidence: (data) => {
      return typeof data.input === 'string';
    },
    disposalHearingQuestionsToExperts: (data) => {
      return data.date.match(/\d{4}-\d{2}-\d{2}/);
    },
    fastTrackTrial: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string';
    },
    smallClaimsJudgesRecital: (data) => {
      return typeof data.input === 'string';
    },
    fastTrackCreditHire: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string'
        && typeof data.input5 === 'string'
        && typeof data.input6 === 'string'
        && typeof data.input7 === 'string'
        && typeof data.input8 === 'string';
    },
    smallClaimsMethodInPerson: (data) => {
      return data.value.code && data.value.label && Array.isArray(data.list_items);
    },
    smallClaimsNotes: (data) => {
      return typeof data.input === 'string';
    },
    fastTrackRoadTrafficAccident: (data) => {
      return typeof data.input === 'string';
    },
    disposalHearingJudgesRecital: (data) => {
      return typeof data.input === 'string';
    },
    disposalHearingMethodInPerson: (data) => {
      return data.value.code && data.value.label && Array.isArray(data.list_items);
    },
    fastTrackSchedulesOfLossToggle: (data) => Array.isArray(data),

    fastTrackMethod: (data) => typeof data === 'string',
    fastTrackJudgesRecital: (data) => {
      return typeof data.input === 'string';
    },
    fastTrackHousingDisrepair: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string';
    },
    disposalHearingDisclosureOfDocuments: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string';
    },
    smallClaimsRoadTrafficAccident:(data) => {
      return typeof data.input === 'string';
    },
    fastTrackCostsToggle: (data) => Array.isArray(data),
    smallClaimsDocumentsToggle: (data) => Array.isArray(data),
    fastTrackVariationOfDirectionsToggle: (data) => Array.isArray(data),
    disposalHearingWitnessOfFact: (data) => {
      return typeof data.input3 === 'string'
        && typeof data.input4 === 'string'
        && typeof data.input5 === 'string'
        && typeof data.input6 === 'string';
    },
    disposalHearingQuestionsToExpertsToggle: (data) => Array.isArray(data),
    smallClaimsDocuments: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string';
    },
    smallClaimsMethodToggle: (data) => Array.isArray(data),
    disposalHearingClaimSettlingToggle: (data) => Array.isArray(data)
  }
};

const calculatedClaimsTrackWSum = {
  ClaimsTrack: {...calculatedClaimsTrackWOSum.ClaimsTrack,
    drawDirectionsOrder: (data) => {
      return typeof data.judgementSum === 'string';
    }
  }
};

const calculatedClaimsTrackCarmEnabled = {
  ClaimsTrack: {...calculatedClaimsTrackWOSum.ClaimsTrack,
    smallClaimsMediationSectionToggle: (data) => Array.isArray(data)
  }
};

const welshLanFields = {
  sdoR2DrhUseOfWelshIncludeInOrderToggle: (data) => Array.isArray(data),
  sdoR2DrhUseOfWelshLanguage: (data) => {
    return typeof data.description === 'string';
  },
  sdoR2SmallClaimsUseOfWelshLanguage: (data) => {
    return typeof data.description === 'string';
  },
  sdoR2FastTrackUseOfWelshLanguage: (data) => {
    return typeof data.description === 'string';
  },
  sdoR2DisposalHearingUseOfWelshLanguage: (data) => {
    return typeof data.description === 'string';
  }
};

const calculatedClaimsTrackDRH = {
  ClaimsTrack: {...calculatedClaimsTrackWOSum.ClaimsTrack,
    ...welshLanFields,
    disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
    fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
    fastTrackHearingTime: (d) =>
      d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
    disposalHearingHearingTime: (d) =>
      d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
      && d.dateTo,
    sdoR2SmallClaimsJudgesRecital: (data) => {
      return typeof data.input === 'string';
    },
    sdoR2SmallClaimsPPIToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsWitnessStatementsToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsUploadDocToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsHearingToggle: (data) => Array.isArray(data),
    sdoR2SmallClaimsWitnessStatements: (data) => {
      return typeof data.sdoStatementOfWitness === 'string'
      && typeof data.isRestrictWitness === 'string'
      && typeof data.isRestrictPages === 'string'
        && typeof data.text === 'string';
    },
    sdoR2SmallClaimsUploadDoc: (data) => {
      return typeof data.sdoUploadOfDocumentsTxt === 'string';
    },
    sdoR2SmallClaimsHearing: (data) => {
      return typeof data.trialOnOptions === 'string'
      && typeof data.hearingCourtLocationList === 'object'
      && typeof data.methodOfHearing === 'string'
        && typeof data.physicalBundleOptions === 'string'
        && typeof data.sdoR2SmallClaimsHearingFirstOpenDateAfter.listFrom.match(/\d{4}-\d{2}-\d{2}/);
    },
    sdoR2SmallClaimsImpNotes: (data) => {
      return typeof data.text === 'string'
      && typeof data.date.match(/\d{4}-\d{2}-\d{2}/);
    },
    sdoR2SmallClaimsPPI: (data) => {
      return typeof data.ppiDate.match(/\d{4}-\d{2}-\d{2}/)
       && typeof data.text === 'string';
    }
  }
};

const calculatedClaimsTrackDRHCarm = {
  ClaimsTrack: {...calculatedClaimsTrackDRH.ClaimsTrack,
    sdoR2SmallClaimsMediationSectionToggle: (data) => Array.isArray(data),
  }
};

//Disposal Hearing
module.exports = {

  createSDODisposal: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
          drawDirectionsOrder: {
            judgementSum: '20'
          }
        },
        ClaimsTrack: {
          drawDirectionsOrderSmallClaims: 'No'
        },
        OrderType: {
          orderType: 'DISPOSAL'
        },
        DisposalHearing: {
          disposalHearingJudgesRecital: {
            input: 'string'
          },
          disposalHearingDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1)
          },
          disposalHearingWitnessOfFact: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            input3: 'string',
            date2: date(1),
            input4: 'string',
            input5: 'string',
            date3: date(1),
            input6: 'string'
          },
          disposalHearingMedicalEvidence: {
            input: 'string',
            date: date(1)
          },
          disposalHearingQuestionsToExperts: {
            date: date(1)
          },
          disposalHearingSchedulesOfLoss: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string',
            date3: date(1),
            input4: 'string',
            date4: date(1)
          },
          disposalHearingFinalDisposalHearing: {
            input: 'string',
            date: date(1),
            time: 'FIFTEEN_MINUTES'
          },
          disposalHearingMethod: 'disposalHearingMethodTelephoneHearing',
          disposalHearingMethodTelephoneHearing: 'telephoneTheClaimant',
          disposalHearingBundle: {
            input: '',
            type: [
              'DOCUMENTS',
              'SUMMARY'
            ]
          },
          disposalHearingAddNewDirections: [
            element({
              directionComment: 'string1'
            }),
            element({
              directionComment: 'string2'
            })
          ],
          disposalHearingNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          // to trigger calculated
        },
        DisposalHearing: {
          // to trigger calculated
        }
      },
      calculated: calculatedClaimsTrackWSum
    };
    const disposalChecks = {
      fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
      disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
      fastTrackHearingTime: (d) =>
        d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
      disposalHearingHearingTime: (d) =>
        d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
        && d.dateTo
    };
    data.calculated.OrderType = data.calculated.ClaimsTrack;
    data.calculated.DisposalHearing = {...data.calculated.ClaimsTrack,
      ...disposalChecks,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    data.calculated.ClaimsTrack = {...data.calculated.ClaimsTrack, ...disposalChecks};
    return data;
  },

  createSDOSmallCarm: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
          drawDirectionsOrder: {
            judgementSum: '20'
          }
        },
        ClaimsTrack: {
          drawDirectionsOrderSmallClaims: 'Yes'
        },
        SmallClaims: {
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodTelephoneHearing',
          smallClaimsMethodTelephoneHearing: 'telephoneTheClaimant',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsWitnessStatement: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string'
          },
          smallClaimsMediationSectionStatement: {
            input : 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackCarmEnabled
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    return data;
  },


//Small Claims WITH Sum of Damages

  createSDOSmall: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
          drawDirectionsOrder: {
            judgementSum: '20'
          }
        },
        ClaimsTrack: {
          drawDirectionsOrderSmallClaims: 'Yes'
        },
        SmallClaims: {
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodTelephoneHearing',
          smallClaimsMethodTelephoneHearing: 'telephoneTheClaimant',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsWitnessStatement: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackWSum
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    return data;
  },


  createLASDO: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
          claimsTrack: 'smallClaimsTrack',
        },
        SmallClaims: {
          smallClaims: [

          ],
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodTelephoneHearing',
          smallClaimsMethodTelephoneHearing: 'telephoneTheClaimant',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsWitnessStatement: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackWSum
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    return data;
  },


//Fast Track WITH Sum of damages

  createSDOFast: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
          drawDirectionsOrder: {
            judgementSum: 20
          },
        },
        ClaimsTrack: {
          drawDirectionsOrderSmallClaims: 'No'
        },
        OrderType: {
          orderType: 'DECIDE_DAMAGES',
          orderTypeTrialAdditionalDirections: [
            'OrderTypeTrialAdditionalDirectionsBuildingDispute',
            'OrderTypeTrialAdditionalDirectionsClinicalNegligence',
            'OrderTypeTrialAdditionalDirectionsCreditHire',
            'OrderTypeTrialAdditionalDirectionsEmployersLiability',
            'OrderTypeTrialAdditionalDirectionsHousingDisrepair',
            'OrderTypeTrialAdditionalDirectionsPersonalInjury',
            'OrderTypeTrialAdditionalDirectionsRoadTrafficAccident',
          ]
        },
        FastTrack: {
          fastTrackJudgesRecital: {
            input: 'string'
          },
          fastTrackAllocation: {
            assignComplexityBand: 'Yes',
            band: 'BAND_2',
            reasons: 'reasons'
          },
          fastTrackDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1),
            input3: 'string',
            input4: 'string',
            date3: date(-1)
          },
          fastTrackWitnessOfFact: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string',
            input5: 'string',
            input6: '1',
            input7: 'string',
            input8: 'string',
            date: date(1),
            input9: 'string'
          },
          fastTrackSchedulesOfLoss: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackTrial: {
            input1: 'string',
            date1: date(1),
            date2: date(1),
            input2: 'string',
            input3: 'string',
            type: ['DOCUMENTS']
          },
          fastTrackMethod: 'fastTrackMethodTelephoneHearing',
          fastTrackMethodTelephoneHearing: 'telephoneTheClaimant',
          fastTrackBuildingDispute: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackClinicalNegligence: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            input4: 'string'
          },
          fastTrackCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input8: 'string'
          },
          fastTrackHousingDisrepair: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackPersonalInjury: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackRoadTrafficAccident: {
            input: 'string',
            date: date(1)
          },
          fastTrackAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          fastTrackNotes: {
            input: 'string',
            date: date(1)
          },
          fastTrackHearingNotes: {
            input: 'Claimant\'s expert will be joining via Video\nRemaining hearing participants will attend in person'
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes'
        },
        FastTrack: {

        }
      },
      calculated: calculatedClaimsTrackWSum
    };
    data.calculated.OrderType = {...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.FastTrack = {...data.calculated.OrderType,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  },

  createSDOFastInPerson: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
          drawDirectionsOrder: {
            judgementSum: 20
          },
        },
        ClaimsTrack: {
          drawDirectionsOrderSmallClaims: 'No'
        },
        OrderType: {
          orderType: 'DECIDE_DAMAGES',
          orderTypeTrialAdditionalDirections: [
            'OrderTypeTrialAdditionalDirectionsBuildingDispute',
            'OrderTypeTrialAdditionalDirectionsClinicalNegligence',
            'OrderTypeTrialAdditionalDirectionsCreditHire',
            'OrderTypeTrialAdditionalDirectionsEmployersLiability',
            'OrderTypeTrialAdditionalDirectionsHousingDisrepair',
            'OrderTypeTrialAdditionalDirectionsPersonalInjury',
            'OrderTypeTrialAdditionalDirectionsRoadTrafficAccident',
          ]
        },
        FastTrack: {
          fastTrackJudgesRecital: {
            input: 'string'
          },
          fastTrackAllocation: {
            assignComplexityBand: 'Yes',
            band: 'BAND_2',
            reasons: 'reasons'
          },
          fastTrackDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1),
            input3: 'string',
            input4: 'string',
            date3: date(-1)
          },
          fastTrackWitnessOfFact: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string',
            input5: 'string',
            input6: '1',
            input7: 'string',
            input8: 'string',
            date: date(1),
            input9: 'string'
          },
          fastTrackSchedulesOfLoss: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackTrial: {
            input1: 'string',
            date1: date(1),
            date2: date(1),
            input2: 'string',
            input3: 'string',
            type: ['DOCUMENTS']
          },
          fastTrackMethod: 'fastTrackMethodInPerson',
          fastTrackBuildingDispute: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackClinicalNegligence: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            input4: 'string'
          },
          fastTrackCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input8: 'string'
          },
          fastTrackHousingDisrepair: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackPersonalInjury: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackRoadTrafficAccident: {
            input: 'string',
            date: date(1)
          },
          fastTrackAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          fastTrackNotes: {
            input: 'string',
            date: date(1)
          },
          fastTrackHearingNotes: {
            input: 'Claimant\'s expert will be joining via Video\nRemaining hearing participants will attend in person'
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes'
        },
        FastTrack: {

        }
      },
      calculated: calculatedClaimsTrackWSum
    };
    data.calculated.OrderType = {...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.FastTrack = {...data.calculated.OrderType,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  },

//Small Claims WITHOUT Sum of Damages

  createSDOSmallWODamageSum: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
        },
        ClaimsTrack: {
          claimsTrack: 'smallClaimsTrack',
          smallClaims: [
            'smallClaimCreditHire',
            'smallClaimRoadTrafficAccident'
          ],
        },
        SmallClaims: {
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodTelephoneHearing',
          smallClaimsMethodTelephoneHearing: 'telephoneTheClaimant',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsWitnessStatement: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string'
          },
          smallClaimsCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input11: 'string'
          },
          smallClaimsRoadTrafficAccident: {
            input: 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackWOSum
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    const disposalChecks = {
      fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
      disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
      fastTrackHearingTime: (d) =>
        d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
      disposalHearingHearingTime: (d) =>
        d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
        && d.dateTo
    };
    data.calculated.ClaimsTrack = {
      ...data.calculated.ClaimsTrack,
      ...disposalChecks
    };
    data.calculated.SmallClaims = {
      ...data.calculated.SmallClaims,
      ...disposalChecks
    };
    return data;
  },

  //Small Claims FlightDelay WITHOUT Sum of Damages

  createSDOSmallFlightDelayWODamageSum: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
        },
        ClaimsTrack: {
          claimsTrack: 'smallClaimsTrack',
          smallClaims: [
            'smallClaimFlightDelay'
          ],
        },
        SmallClaims: {
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodTelephoneHearing',
          smallClaimsMethodTelephoneHearing: 'telephoneTheClaimant',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsFlightDelay: {
            relatedClaimsInput: 'string',
            legalDocumentsInput: 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackWOSum
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    const disposalChecks = {
      fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
      disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
      fastTrackHearingTime: (d) =>
        d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
      disposalHearingHearingTime: (d) =>
        d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
        && d.dateTo
    };
    data.calculated.ClaimsTrack = {
      ...data.calculated.ClaimsTrack,
      ...disposalChecks
    };
    data.calculated.SmallClaims = {
      ...data.calculated.SmallClaims,
      ...disposalChecks
    };
    return data;
  },


  //Small Claims WITHOUT Sum of Damages in person

  createSDOSmallWODamageSumInPerson: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
        },
        ClaimsTrack: {
          claimsTrack: 'smallClaimsTrack',
          smallClaims: [
            'smallClaimCreditHire',
            'smallClaimRoadTrafficAccident'
          ],
        },
        SmallClaims: {
          smallClaimsJudgesRecital: {
            input: 'string'
          },
          smallClaimsHearing: {
            input1: 'string',
            input2: 'string',
            time: 'THIRTY_MINUTES'
          },
          smallClaimsMethod: 'smallClaimsMethodInPerson',
          smallClaimsDocuments: {
            input1: 'string',
            input2: 'string'
          },
          smallClaimsWitnessStatement: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string'
          },
          smallClaimsCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input11: 'string'
          },
          smallClaimsRoadTrafficAccident: {
            input: 'string'
          },
          smallClaimsAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          smallClaimsNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackWOSum
    };
    data.calculated.SmallClaims = {...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.ClaimsTrack.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.ClaimsTrack.setFastTrackFlag
    };
    const disposalChecks = {
      fastTrackOrderWithoutJudgement: (d) => typeof d.input === 'string',
      disposalOrderWithoutHearing: (d) => typeof d.input === 'string',
      fastTrackHearingTime: (d) =>
        d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.',
      disposalHearingHearingTime: (d) =>
        d.input === 'This claim will be listed for final disposal before a judge on the first available date after'
        && d.dateTo
    };
    data.calculated.ClaimsTrack = {
      ...data.calculated.ClaimsTrack,
      ...disposalChecks
    };
    data.calculated.SmallClaims = {
      ...data.calculated.SmallClaims,
      ...disposalChecks
    };
    return data;
  },

//Fast Track WITHOUT Sum of damages

  createSDOFastWODamageSum: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
        },
        ClaimsTrack: {
          claimsTrack: 'fastTrack',
          drawDirectionsOrderSmallClaims: 'No',
          fastClaims: [
            'fastClaimBuildingDispute',
            'fastClaimClinicalNegligence',
            'fastClaimCreditHire',
            'fastClaimEmployersLiability',
            'fastClaimHousingDisrepair',
            'fastClaimPersonalInjury',
            'fastClaimRoadTrafficAccident'
          ]
        },
        OrderType: {
          orderType: 'DECIDE_DAMAGES'
        },
        FastTrack: {
          fastTrackJudgesRecital: {
            input: 'string'
          },
          fastTrackAllocation: {
            assignComplexityBand: 'Yes',
            band: 'BAND_2',
            reasons: 'reasons'
          },
          fastTrackDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1),
            input3: 'string',
            input4: 'string',
            date3: date(-1)
          },
          fastTrackWitnessOfFact: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string',
            input5: 'string',
            input6: '1',
            input7: 'string',
            input8: 'string',
            date: date(1),
            input9: 'string'
          },
          fastTrackSchedulesOfLoss: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackTrial: {
            input1: 'string',
            date1: date(1),
            date2: date(1),
            input2: 'string',
            input3: 'string',
            type: ['DOCUMENTS']
          },
          fastTrackMethod: 'fastTrackMethodTelephoneHearing',
          fastTrackMethodTelephoneHearing: 'telephoneTheClaimant',
          fastTrackBuildingDispute: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackClinicalNegligence: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            input4: 'string'
          },
          fastTrackCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input8: 'string'
          },
          fastTrackHousingDisrepair: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackPersonalInjury: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackRoadTrafficAccident: {
            input: 'string',
            date: date(1)
          },
          fastTrackAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          fastTrackNotes: {
            input: 'string',
            date: date(1)
          }
        }
      },
      midEventData: {
        ClaimsTrack: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes'
        },
        FastTrack: {

        }
      },
      calculated: calculatedClaimsTrackWOSum
    };
    data.calculated.OrderType = {...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.FastTrack = {...data.calculated.OrderType,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  },

  //Fast track for spec
  //Fast Track WITHOUT Sum of damages

  createSDOFastTrackSpec: () => {

    const createSDO = {};
    createSDO.valid = {
      ...createSDO.valid,
        SDO: {
          drawDirectionsOrderRequired: 'Yes',
        },
        ClaimsTrack: {
          claimsTrack: 'fastTrack',
          drawDirectionsOrderSmallClaims: 'No',
          fastClaims: [
            'fastClaimBuildingDispute',
            'fastClaimClinicalNegligence',
            'fastClaimCreditHire',
            'fastClaimEmployersLiability',
            'fastClaimHousingDisrepair',
            'fastClaimPersonalInjury',
            'fastClaimRoadTrafficAccident'
          ]
        },
        OrderType: {
          orderType: 'DECIDE_DAMAGES'
        },
        FastTrack: {
          fastTrackJudgesRecital: {
            input: 'string'
          },
          fastTrackAllocation: {
            assignComplexityBand: 'Yes',
            band: 'BAND_2',
            reasons: 'reasons'
          },
          fastTrackDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1),
            input3: 'string',
            input4: 'string',
            date3: date(-1)
          },
          fastTrackWitnessOfFact: {
            input1: 'string',
            input2: '1',
            input3: '1',
            input4: 'string',
            input5: 'string',
            input6: '1',
            input7: 'string',
            input8: 'string',
            date: date(1),
            input9: 'string'
          },
          fastTrackSchedulesOfLoss: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackTrial: {
            input1: 'string',
            date1: date(1),
            date2: date(1),
            input2: 'string',
            input3: 'string',
            type: ['DOCUMENTS']
          },
          fastTrackMethod: 'fastTrackMethodTelephoneHearing',
          fastTrackMethodTelephoneHearing: 'telephoneTheClaimant',
          fastTrackBuildingDispute: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackClinicalNegligence: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            input4: 'string'
          },
          fastTrackCreditHire: {
            input1: 'string',
            input2: 'string',
            date1: date(1),
            input3: 'string',
            input4: 'string',
            date2: date(1),
            input5: 'string',
            input6: 'string',
            date3: date(1),
            input7: 'string',
            date4: date(1),
            input8: 'string'
          },
          fastTrackHousingDisrepair: {
            input1: 'string',
            input2: 'string',
            input3: 'string',
            date1: date(1),
            input4: 'string',
            date2: date(1)
          },
          fastTrackPersonalInjury: {
            input1: 'string',
            date1: date(1),
            input2: 'string',
            date2: date(1),
            input3: 'string'
          },
          fastTrackRoadTrafficAccident: {
            input: 'string',
            date: date(1)
          },
          fastTrackAddNewDirections: [
            element({
              directionComment: 'string'
            }),
            element({
              directionComment: 'string'
            })
          ],
          fastTrackNotes: {
            input: 'string',
            date: date(1)
          }
        }
      };
    return createSDO;
  },

//Unsuitable for SDO

  createNotSuitableSDO: () => {
    return {
      valid: {
        NotSuitableSDO: {
          reasonNotSuitableSDO: {
            input: 'Too many problems.'
          }
        }
      }
    };
  },

  createSDOFastNIHL: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
          drawDirectionsOrder: {
            judgementSum: 20
          },
        },
        ClaimsTrack: {
          claimsTrack: 'fastTrack',
          drawDirectionsOrderSmallClaims: 'No',
          fastClaims: [
            'fastClaimNoiseInducedHearingLoss'
          ],
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes',
          isSdoR2NewScreen: 'Yes',
        },
        SdoR2FastTrack: {
          sdoAltDisputeResolution: {
            includeInOrderToggle: [
              'INCLUDE'
            ]
          },
          sdoFastTrackJudgesRecital: {
            input: 'Upon considering the statements of case and the information provided by the parties.'
          },
          sdoR2AddendumReport: {
            sdoAddendumReportDate: date(+56),
            sdoAddendumReportTxt: 'The Claimant may upload to the Digital Portal an addendum report from their expert ENT surgeon by 4pm on'
          },
          sdoR2DisclosureOfDocuments: {
            inspectionDate: date(+42),
            inspectionTxt: 'Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on',
            requestsWillBeCompiledLabel: 'within 7 days of receipt.',
            standardDisclosureDate: date(+28),
            standardDisclosureTxt: 'standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on'
          },
          sdoR2DisclosureOfDocumentsToggle: [
            'INCLUDE'
          ],
          sdoR2EvidenceAcousticEngineer: {
            sdoEvidenceAcousticEngineerTxt: 'The parties have permission to rely on the jointly instructed written evidence of an expert acoustic engineer.',
            sdoExpertReportDate: date(+280),
            sdoExpertReportDigitalPortalTxt: 'by the Claimant within 7 days of receipt.',
            sdoExpertReportTxt: 'The expert will report to the instructing parties by',
            sdoInstructionOfTheExpertDate: date(+42),
            sdoInstructionOfTheExpertTxt: 'The expert shall be agreed and instructed by',
            sdoInstructionOfTheExpertTxtArea: 'if no expert has been instructed by the date the Claimant must apply to court by 4pm the following day for further directions.',
            sdoRepliesDate: date (+315),
            sdoRepliesDigitalPortalTxt: 'by that party within 7 days of receipt.',
            sdoRepliesTxt: 'send the answers to questions to the asking party by',
            sdoServiceOfOrderTxt: 'A copy of this order must be served on the expert by the Claimant with the experts instructions.',
            sdoWrittenQuestionsDate: date(+336),
            sdoWrittenQuestionsDigitalPortalTxt: 'by the same date.',
            sdoWrittenQuestionsTxt: 'Written questions may be posed by any party directly to the single jointly instructed expert by'
          },
          sdoR2ExpertEvidence: {
            sdoClaimantPermissionToRelyTxt: 'The Claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal with the particulars of claim.'
          },
          sdoR2FurtherAudiogram: {
            sdoClaimantShallUndergoDate: date(+42),
            sdoClaimantShallUndergoTxt: 'The Claimant shall undergo a single further audiogram at the written request of any Defendant. Such request to be made no later than 4pm on',
            sdoServiceReportDate: date(+98),
            sdoServiceReportTxt: 'The further audiogram shall be arranged and paid for by the Defendant requesting it. The Defendant shall serve a copy of the further audiogram on the Claimant and upload to the Digital Portal by 4pm on'
          },
          sdoR2ImportantNotesDate: date(+7),
          sdoR2ImportantNotesTxt: 'This Order has been made without hearing. Each party has the right to apply to have this Order set aside or varied. Any such application must be received by the Court (together with the appropriate fee) by 4pm on',
          sdoR2PermissionToRelyOnExpert: {
            sdoJointMeetingOfExpertsDate: date(+147),
            sdoJointMeetingOfExpertsTxt: 'The experts instructed by each party shall discuss their reports and shall prepare a schedule of agreement and disagreement which shall be provided to the parties by 4pm on',
            sdoPermissionToRelyOnExpertDate: date(+119),
            sdoPermissionToRelyOnExpertTxt: 'The Defendant has permission to rely on written expert evidence from a consultant ENT surgeon. Such report shall be uploaded to the Digital Portal by 4pm on',
            sdoUploadedToDigitalPortalTxt: 'by the Claimant within 7 days of receipt.'
          },
          sdoR2QuestionsClaimantExpert: {
            sdoApplicationToRelyOnFurther: {
              doRequireApplicationToRely: 'No'
            },
            sdoDefendantMayAskDate: date(+126),
            sdoDefendantMayAskTxt: 'The Defendant(s) may ask questions of the Claimants expert which must be sent to the expert directly and uploaded to th Digital Portal by 4pm on',
            sdoQuestionsShallBeAnsweredDate: date(+147),
            sdoQuestionsShallBeAnsweredTxt: 'The questions shall be answered by the expert by',
            sdoUploadedToDigitalPortalTxt: 'by the asking party within 7 days of receipt.'
          },
          sdoR2QuestionsToEntExpert: {
            sdoQuestionsShallBeAnsweredDate: date(+350),
            sdoQuestionsShallBeAnsweredTxt: 'such questions shall be answered by the ENT expert by',
            sdoShallBeUploadedTxt: 'within 7 days of receipt.',
            sdoWrittenQuestionsDate: date(+336),
            sdoWrittenQuestionsDigPortalTxt: 'and shall upload the same to the Digital Portal by the same date.\nSuch questions shall be limited to issues arising from the single jointly instructed expert engineers report and any answers to questions.',
            sdoWrittenQuestionsTxt: 'The parties may put written questions of an ENT engineering expert for whom permission has been given by 4pm on'
          },
          sdoR2ScheduleOfLoss: {
            isClaimForPecuniaryLoss: 'No',
            sdoR2ScheduleOfLossClaimantDate: date(+364),
            sdoR2ScheduleOfLossClaimantText: 'The Claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on',
            sdoR2ScheduleOfLossDefendantDate: date(+378),
            sdoR2ScheduleOfLossDefendantText: 'in the event of a challenge to the updated schedule of loss, a defendant shall upload to the Digital Portal a counter-schedule by 4pm on'
          },
          sdoR2ScheduleOfLossToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorAddendumReportToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorEvidenceAcousticEngineerToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorExpertEvidenceToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorFurtherAudiogramToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorPermissionToRelyOnExpertToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorQuestionsClaimantExpertToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorQuestionsToEntExpertToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorUploadOfDocumentsToggle: [
            'INCLUDE'
          ],
          sdoR2SeparatorWitnessesOfFactToggle: [
            'INCLUDE'
          ],
          sdoR2Settlement: {
            includeInOrderToggle: [
              'INCLUDE'
            ]
          },
          sdoR2Trial: {
            hearingCourtLocationList: {
              list_items: [
                {
                  code: '000000',
                  label: 'Barnet Civil and Family Centre - St Marys Court, Regents Park Road - N3 1BQ'
                },
                {
                  code: 'OTHER_LOCATION',
                  label: 'Other location'
                }
              ],
              value: {
                code: '000000',
                label: 'Barnet Civil and Family Centre - St Marys Court, Regents Park Road - N3 1BQ'
              }
            },
            lengthList: 'FIVE_HOURS',
            physicalBundleOptions: 'NONE',
            sdoR2TrialFirstOpenDateAfter: {
              listFrom: date(434)
            },
            trialOnOptions: 'OPEN_DATE'
          },
          sdoR2TrialToggle: [
            'INCLUDE'
          ],
          sdoR2UploadOfDocuments: {
            sdoUploadOfDocumentsTxt: 'Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial 21 days before the hearing.'
          },
          sdoR2WitnessesOfFact: {
            sdoR2RestrictWitness: {
              isRestrictWitness: 'No'
            },
            sdoRestrictPages: {
              isRestrictPages: 'No'
            },
            sdoStatementOfWitness: 'Each party must upload to the Digital Portal copies of the statements of all witnesses of fact on whom they intend to rely.',
            sdoWitnessDeadline: 'Witness statements shall be uploaded to the Digital Portal by 4pm on',
            sdoWitnessDeadlineDate: date(+70),
            sdoWitnessDeadlineText: 'Evidence will not be permitted at trial from a witness whose statement has not been uploaded in accordance with the Order, except with permission from the Court.'
          },
          sdoVariationOfDirections: {
            includeInOrderToggle: [
              'INCLUDE'
            ]
          },
          sdoR2NihlUseOfWelshLanguage: {
            description: 'If any party is legally represented then when filing any witness evidence, the legal representatives must notify the Court in writing that:\na) they have advised their client of the entitlement of any party or witness to give evidence in the Welsh Language in accordance with the Welsh Language Act 1993(which is not dependant on whether they are fluent in English)\nb) instructions have been taken as to whether any party or witness will exercise that entitlement, in which case the legal representatives must so inform the Court so that arrangements can be made by the Court for instantaneous translation facilities to be made available without charge\n\nAny unrepresented party or witness for such a party being entitled to give evidence in the Welsh Language in accordance with the principle of the Welsh Language Act 1993 must notify the Court when sending to the Court their witness evidence whether any party or witness will exercise that entitlement whereupon the Court will make arrangements for instantaneous translation facilities to be made available without charge.'
          },
        }
      },
      midEventData: {
        ClaimsTrack: {
          isSdoR2NewScreen : 'Yes',
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes'
        },
        OrderType: {
          setSmallClaimsFlag: 'No',
          setFastTrackFlag: 'Yes'
        },
        FastTrack: {}
      },
      calculated: calculatedClaimsTrackWSum
    };
    data.calculated.OrderType = {
      ...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.ClaimsTrack = {
      ...data.calculated.ClaimsTrack,
      ...welshLanFields
    };
    data.calculated.FastTrack = {
      ...data.calculated.OrderType,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  },

  //DRH
  createSDOSmallDRH: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
          drawDirectionsOrder: {
            judgementSum: 20
          },
        },
        ClaimsTrack: {
          claimsTrack: 'smallClaimsTrack',
          drawDirectionsOrderSmallClaims: 'No',
          smallClaims: [
            'smallClaimDisputeResolutionHearing'
          ],
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'Yes',
          isSdoR2NewScreen: 'Yes'
        },
        SdoR2SmallClaims: {
        },
      },
      midEventData: {
        ClaimsTrack: {
          isSdoR2NewScreen : 'Yes',
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SdoR2SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackDRH
    };
    data.calculated.OrderType = {
      ...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.SdoR2SmallClaims = {
      ...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  },

  //DRHCarm
  createSDOSmallDRHCarm: () => {
    const data = {
      valid: {
        SDO: {
          drawDirectionsOrderRequired: 'No',
          drawDirectionsOrder: {
            judgementSum: 20
          },
        },
        ClaimsTrack: {
          claimsTrack: 'smallClaimsTrack',
          drawDirectionsOrderSmallClaims: 'No',
          smallClaims: [
            'smallClaimDisputeResolutionHearing'
          ],
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'Yes',
          isSdoR2NewScreen: 'Yes'
        },
        SdoR2SmallClaims: {
          sdoR2SmallClaimsMediationSectionToggle: [
            'INCLUDE'
          ],
          sdoR2SmallClaimsMediationSectionStatement: {
            input: 'If you failed to attend a mediation appointment, then the judge at the hearing may impose a '
            + 'sanction. This could require you to pay costs, or could result in your claim or defence being '
            + 'dismissed. You should deliver to every other party, and to the court, your explanation for '
            + 'non-attendance, with any supporting documents, at least 14 days before the hearing. Any other party who '
            + 'wishes to comment on the failure to attend the mediation appointment should deliver their comments, '
            + 'with any supporting documents, to all parties and to the court at least 14 days before the hearing.'
          },        },
      },
      midEventData: {
        ClaimsTrack: {
          isSdoR2NewScreen : 'Yes',
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        OrderType: {
          setSmallClaimsFlag: 'Yes',
          setFastTrackFlag: 'No'
        },
        SdoR2SmallClaims: {
        }
      },
      calculated: calculatedClaimsTrackDRHCarm
    };
    data.calculated.OrderType = {
      ...data.calculated.ClaimsTrack,
      orderTypeTrialAdditionalDirections: (d) => Array.isArray(d)
    };
    data.calculated.SdoR2SmallClaims = {
      ...data.calculated.ClaimsTrack,
      setSmallClaimsFlag: (d) => d === data.midEventData.OrderType.setSmallClaimsFlag,
      setFastTrackFlag: (d) => d === data.midEventData.OrderType.setFastTrackFlag
    };
    return data;
  }
};
