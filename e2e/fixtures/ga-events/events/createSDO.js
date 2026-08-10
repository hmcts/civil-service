const {date, element} = require('../../../api/dataHelper');

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
        && typeof data.input3 === 'string';
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
    smallClaimsCreditHire: (data) => {
      return typeof data.input1 === 'string'
        && typeof data.input2 === 'string'
        && typeof data.input3 === 'string'
        && typeof data.input4 === 'string'
        && typeof data.input5 === 'string'
        && typeof data.input6 === 'string'
        && typeof data.input7 === 'string'
        && typeof data.input8 === 'string'
        && typeof data.input9 === 'string'
        && typeof data.input10 === 'string';
    },
    disposalHearingCostsToggle: (data) => Array.isArray(data),
    smallClaimsWitnessStatementToggle: (data) => Array.isArray(data),
    smallClaimsHearingToggle: (data) => Array.isArray(data),
    fastTrackMethodToggle: (data) => Array.isArray(data),
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
        && typeof data.input2 === 'string'
        && typeof data.deadlineDate.match(/\d{4}-\d{2}-\d{2}/);
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
            date1: date(-1)
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
        d.helpText1 === 'If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date of this order.'
        && d.helpText2 === 'Not more than seven nor less than three clear days before the trial, '
        + 'the claimant must file at court and serve an indexed and paginated bundle of documents which complies with the'
        + ' requirements of Rule 39.5 Civil Procedure Rules and which complies with requirements of PD32. '
        + 'The parties must endeavour to agree the contents of the bundle before it is filed. The bundle will include a case summary and a chronology.',
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
            input2: 'string',
            deadlineDate: date(28)
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
          fastTrackDisclosureOfDocuments: {
            input1: 'string',
            date1: date(-1),
            input2: 'string',
            date2: date(-1),
            input3: 'string'
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
  }
};
