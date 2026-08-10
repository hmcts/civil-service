const {listElement} = require('../../../../api/dataHelper');
const config = require('../../../../config.js');
module.exports = {
  respondToClaim: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC') => {
    const responseData = {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondentCorrespondenceAddressRequired: 'Yes'
        },
      },
    };

    switch (response) {
      case 'FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            claimant1ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            claimant2ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          defenceRoute: {
            defenceRouteRequired: 'DISPUTES_THE_CLAIM'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          Mediation: {
            responseClaimMediationSpecRequired: 'No'
          },
          SmallClaimExperts: {
            responseClaimExpertSpecRequired: 'No'
          },
          SmallClaimWitnesses: {
            responseClaimWitnesses: '10'
          },
          Language: {
            respondent1DQLanguage: {
              evidence: 'ENGLISH',
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          SmaillClaimHearing: {
            SmallClaimHearingInterpreterDescription: 'test',
            SmallClaimHearingInterpreterRequired: 'Yes',
            respondent1DQHearingSmallClaim: {
              unavailableDatesRequired: 'No',
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendant2SelectedCourt)
                ],
                value: listElement(config.defendant2SelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            }
          },
          HearingSupport: {
            respondent1DQHearingSupport: {
              signLanguageRequired: null,
              languageToBeInterpreted: null,
              otherSupport: null,
              requirements: ['DISABLED_ACCESS', 'HEARING_LOOPS']
            }
          },
          VulnerabilityQuestions: {
            respondent1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            respondent1DQHearing: {
              unavailableDatesRequired: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE',
            sameSolicitorSameResponse: null,
            specFullDefenceOrPartAdmission: 'Yes',
            specDefenceFullAdmittedRequired: 'No'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          }
        };
        break;

      case 'FULL_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            claimant1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            claimant2ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            specDefenceFullAdmittedRequired: 'No',
            specDisputesOrPartAdmission: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specDefenceFullAdmittedRequired: 'No'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },
        };
        break;

      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION',
            claimant1ClaimResponseTypeForSpec: 'PART_ADMISSION',
            claimant2ClaimResponseTypeForSpec: 'PART_ADMISSION',
            respondentClaimResponseTypeForSpecGeneric: 'PART_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceAdmittedRequired: 'No',
            respondToAdmittedClaimOwingAmount: '200000'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          Mediation: {
            responseClaimMediationSpecRequired: 'No'
          },
          SmallClaimExperts: {
            responseClaimExpertSpecRequired: 'No'
          },
          SmallClaimWitnesses: {
            responseClaimWitnesses: '10'
          },
          Language: {
            respondent1DQLanguage: {
              evidence: 'ENGLISH',
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          SmaillClaimHearing: {
            SmallClaimHearingInterpreterDescription: 'test',
            SmallClaimHearingInterpreterRequired: 'Yes',
            respondent1DQHearingSmallClaim: {
              unavailableDatesRequired: 'No',
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendant2SelectedCourt)
                ],
                value: listElement(config.defendant2SelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            }
          },
          HearingSupport: {
            respondent1DQHearingSupport: {
              signLanguageRequired: null,
              languageToBeInterpreted: null,
              otherSupport: null,
              requirements: ['DISABLED_ACCESS', 'HEARING_LOOPS']
            }
          },
          VulnerabilityQuestions: {
            respondent1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            respondent1DQHearing: {
              unavailableDatesRequired: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'PART_ADMISSION'
          },

          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondToAdmittedClaimOwingAmountPounds: '2000.00'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },

          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;

      case 'COUNTER_CLAIM':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'COUNTER_CLAIM',
            claimant1ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'NOT_FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'COUNTER_CLAIM'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          }
        };
        break;

      case 'DIFF_FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'No'
          },
          RespondentResponseTypeSpec: {
            claimant1ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            claimant2ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
          defenceRoute: {
            defenceRouteRequired: 'DISPUTES_THE_CLAIM'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          Mediation: {
            responseClaimMediationSpecRequired: 'No'
          },
          SmallClaimExperts: {
            responseClaimExpertSpecRequired: 'No'
          },
          SmallClaimWitnesses: {
            responseClaimWitnesses: '10'
          },
          Language: {
            respondent1DQLanguage: {
              evidence: 'ENGLISH',
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          SmaillClaimHearing: {
            SmallClaimHearingInterpreterDescription: 'test',
            SmallClaimHearingInterpreterRequired: 'Yes',
            respondent1DQHearingSmallClaim: {
              unavailableDatesRequired: 'No',
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendant2SelectedCourt)
                ],
                value: listElement(config.defendant2SelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            }
          },
          HearingSupport: {
            respondent1DQHearingSupport: {
              signLanguageRequired: null,
              languageToBeInterpreted: null,
              otherSupport: null,
              requirements: ['DISABLED_ACCESS', 'HEARING_LOOPS']
            }
          },
          VulnerabilityQuestions: {
            respondent1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            respondent1DQHearing: {
              unavailableDatesRequired: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specFullDefenceOrPartAdmission: 'No',
            specDefenceFullAdmittedRequired: 'No'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          }
        };
        break;

      case 'DIFF_NOT_FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse2v1: {
            defendantSingleResponseToBothClaimants: 'No'
          },
          RespondentResponseTypeSpec: {
            claimant1ClaimResponseTypeForSpec: 'COUNTER_CLAIM',
            claimant2ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specDefenceFullAdmittedRequired: 'No'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          }
        };
        break;
    }

    return responseData;
  }
};
