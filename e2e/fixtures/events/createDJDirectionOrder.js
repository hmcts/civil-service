const {date} = require('../../api/dataHelper');

const djOrderCaseManagementOrderSelection = (djOrderType = 'DISPOSAL_HEARING') => {
  if (djOrderType == 'DISPOSAL_HEARING') {
    return {
      caseManagementOrderSelection: 'DISPOSAL_HEARING',
    };
  } else {
     return {
      caseManagementOrderSelection: 'TRIAL_HEARING',
      caseManagementOrderAdditional: []
     };
  }
};

const createDJDirectionOrder = (djOrderType = 'DISPOSAL_HEARING', mpScenario = 'ONE_V_ONE') => {
    const userInput = (djOrderType = 'DISPOSAL_HEARING') => {
      if (djOrderType == 'DISPOSAL_HEARING') {
        return {
          ...disposalHearing
        };
      } else {
        return {
          ...trialHearing
        };
      }
    };

    const trialHearing = {
      trialHearing: {
        trialHearingAddNewDirectionsDJ: [],
        trialHearingAlternativeDisputeDJToggle: ['SHOW'],
        trialHearingCostsToggle: ['SHOW'],
        trialHearingDisclosureOfDocumentsDJ: {
          date1: date(28),
          date2: date(43),
          date3: date(28),
          input1: 'string',
          input2: 'string',
          input3: 'string',
          input4: 'string',
          input5: 'string'
        },
        trialHearingDisclosureOfDocumentsDJToggle: ['SHOW'],
        trialHearingJudgesRecitalDJ: {
          input: 'string',
          judgeNameTitle: 'title'
        },
        trialHearingMethodDJ: 'disposalHearingMethodInPerson',
        trialOrderMadeWithoutHearingDJ: {
          input: 'string'
        },
        trialHearingSchedulesOfLossDJ: {
          date1: date(70),
          date2: date(84),
          input1: 'string',
          input2: 'string',
          input3: 'string'
        },
        trialHearingSchedulesOfLossDJToggle:['SHOW'],
        trialHearingSettlementDJToggle: ['SHOW'],
        trialHearingTimeDJ: {
          date1: date(34),
          date2: null,
          hearingTimeEstimate: 'ONE_HOUR',
          helpText1: 'string',
          helpText2: 'string'
        },
        trialHearingTrialDJToggle: ['SHOW'],
        trialHearingVariationsDirectionsDJToggle: ['SHOW'],
        trialHearingWitnessOfFactDJ: {
          date1: date(55),
          input1: 'string',
          input2: 4,
          input3: 4,
          input4: 'string',
          input5: 'string',
          input6: 4,
          input7: 'string',
          input8: 'string',
          input9: 'string'
        },
        trialHearingWitnessOfFactDJToggle: ['SHOW'],
        trialHearingHearingNotesDJ: {
          input: 'Hearing notes'
        },
        trialHearingMethodInPersonDJ: {
          value: { code: '420219'}
        }
      }
    };

    const disposalHearing = {
      disposalHearing : {
        disposalHearingAddNewDirectionsDJ: [],
        disposalHearingBundleDJ: {
          input: 'string',
          type: ['DOCUMENTS', 'ELECTRONIC', 'SUMMARY']
        },
        disposalHearingBundleDJToggle: ['SHOW'],
        disposalHearingClaimSettlingDJToggle: ['SHOW'],
        disposalHearingDisclosureOfDocumentsDJToggle: ['SHOW'],
        disposalHearingCostsDJToggle: ['SHOW'],
        disposalHearingDisclosureOfDocumentsDJ: {
          date: date(56),
          input: 'string'
        },
        disposalHearingFinalDisposalHearingTimeDJ: {
          date: date(56),
          input: 'string',
          time: 'THIRTY_MINUTES'
        },
        disposalHearingFinalDisposalHearingDJToggle: ['SHOW'],
        disposalHearingHearingNotesDJ: {
          input: 'Hearing notes'
        },
        disposalHearingJudgesRecitalDJ: {
          input: 'string',
          judgeNameTitle: 'title'
        },
        disposalHearingMedicalEvidenceDJ: {
          date1: date(28),
          input1: 'string'
        },
        disposalHearingMedicalEvidenceDJToggle: ['SHOW'],
        disposalHearingMethodDJ: 'disposalHearingMethodInPerson',
        disposalHearingOrderAndHearingDetailsDJ: {},
        disposalHearingOrderMadeWithoutHearingDJ: {
          input: 'string'
        },
        disposalHearingQuestionsToExpertsDJToggle: ['SHOW'],
        disposalHearingQuestionsToExpertsDJ: {
          date: date(56)
        },
        disposalHearingSchedulesOfLossDJ: {
          date1: date(40),
          date2: date(80),
          date3: date(80),
          input1: 'string',
          input2: 'string',
          input3: 'string',
          inputText4: 'string'
        },
        disposalHearingSchedulesOfLossDJToggle: ['SHOW'],
        disposalHearingWitnessOfFactDJ: {
          date1: date(28),
          date2: date(28),
          input1: 'string',
          input2: 'string',
          input3: 'string',
          input4: 'string'
        },
        disposalHearingWitnessOfFactDJToggle: ['SHOW'],
        disposalHearingMethodInPersonDJ: {
          value: { code: '420219'}
        }
      }
    };
    switch (mpScenario) {
      case 'ONE_V_TWO_TWO_LEGAL_REP':
      case 'ONE_V_TWO_ONE_LEGAL_REP': {
        return {
          djOrderCaseManagementOrderSelection: {
            applicantVRespondentText: 'Test Inc v Sir John Doe and Dr Foo Bar',
            ...djOrderCaseManagementOrderSelection(djOrderType)
          },
          ...userInput(djOrderType)
        };
      }
      case 'TWO_V_ONE':{
        return {
          djOrderCaseManagementOrderSelection: {
            applicantVRespondentText: 'Test Inc and Dr Jane Doe v Sir John Doe',
            ...djOrderCaseManagementOrderSelection(djOrderType)
          },
          ...userInput(djOrderType)
        };
      }
      case 'ONE_V_ONE':
      default: {
        return {
          djOrderCaseManagementOrderSelection: {
            applicantVRespondentText: 'Test Inc v Sir John Doe',
            ...djOrderCaseManagementOrderSelection(djOrderType)
          },
          ...userInput(djOrderType)
        };
      }
    }
};

module.exports = {
  judgeCreateOrder: (djOrderType, mpScenario = 'ONE_V_ONE') => {
    return {
      valid: createDJDirectionOrder(djOrderType, mpScenario)
    };
  }
};
