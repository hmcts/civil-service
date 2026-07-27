const {listElement, element, date} = require('../../api/dataHelper');
const config = require('../../config.js');
module.exports = {
  respondToClaim: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC', fastTrack = false,
                   isMintiEnabled = 'FALSE') => {

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
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          defenceRoute: {
            defenceRouteRequired: 'DISPUTES_THE_CLAIM'
          },
          Mediation: {
            responseClaimMediationSpecRequired: 'No'
          },
          ...(fastTrack ? {
            FixedRecoverableCosts: {
              respondent1DQFixedRecoverableCosts: {
                isSubjectToFixedRecoverableCostRegime: 'Yes',
                band: 'BAND_4',
                complexityBandingAgreed: 'Yes',
                reasons: 'some reasons'
              }
            },
            DisclosureOfElectronicDocumentsLRspec: {
              specRespondent1DQDisclosureOfElectronicDocuments: {
                reachedAgreement: 'No',
                agreementLikely: 'No',
                reasonForNoAgreement: 'example specific reasons'
              }
            },
            DisclosureOfNonElectronicDocumentsLRspec: {
              specRespondent1DQDisclosureOfNonElectronicDocuments: {
                bespokeDirections: 'Non electric document text'
              }
            },
          } : {}),
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent1: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 1'
            }
          },
          SmallClaimExperts: {
            respondent1DQExperts: {
              expertRequired: 'Yes',
              expertReportsSent: 'NOT_OBTAINED',
              jointExpertSuitable: 'Yes',
              details: [
                element({
                  firstName: 'John',
                  lastName: 'Doe',
                  emailAddress: 'john@doemail.com',
                  phoneNumber: '07111111111',
                  fieldOfExpertise: 'None',
                  whyRequired: 'Testing',
                  estimatedCost: '10000'
                })
              ]
            }
          },
          SmallClaimWitnesses: {
            respondent1DQWitnessesSmallClaim: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  firstName: 'Witness',
                  lastName: 'One',
                  emailAddress: 'witness@email.com',
                  phoneNumber: '07116778998',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          Language: {
            respondent1DQLanguage: {
              court: 'WELSH',
              documents: 'WELSH'
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
                  listElement(config.defendantSelectedCourt)
                ],
                value: listElement()
              },
              reasonForHearingAtSpecificCourt: 'Reasons',
              caseLocation: {
                region: '2',
                baseLocation: '420219'
              }
            },
            respondent1DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          HearingSupport: {
            respondent1DQHearingSupport: {
              supportRequirements: 'Yes',
              supportRequirementsAdditional: 'Additional support reasons'
            },
            respondent1DQHearing: {
              unavailableDatesRequired: 'No'
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
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE'
          },
          ...(isMintiEnabled === undefined || isMintiEnabled === 'FALSE') ? {
            defenceRoute: {
              responseClaimTrack: 'SMALL_CLAIM',
              respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
            }
          }: {},
          ...(isMintiEnabled !== undefined && isMintiEnabled === 'MULTI_CLAIM') ? {
            defenceRoute: {
              responseClaimTrack: 'MULTI_CLAIM',
              respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
            }
          }: {},
          ...(isMintiEnabled !== undefined && isMintiEnabled === 'INTERMEDIATE_CLAIM') ? {
            defenceRoute: {
              responseClaimTrack: 'INTERMEDIATE_CLAIM',
              respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
            }
          }: {}
        };
        break;
      case 'FULL_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceFullAdmittedRequired: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          },
          defenceRoute: {
            specPaidLessAmountOrDisputesOrPartAdmission: 'No',
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'FULL_ADMISSION',
            specDefenceFullAdmittedRequired: 'No'
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceAdmittedRequired: 'No',
            respondToAdmittedClaimOwingAmount: '200000'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          FileDirectionsQuestionnaire: {
            respondent1DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'Yes',
              reactionProtocolCompliedWith: 'Yes'
            }
          },
          DisclosureOfElectronicDocumentsLRspec: {
            specRespondent1DQDisclosureOfElectronicDocuments: {
              reachedAgreement: 'Yes'
            }
          },
          Experts: {
            respondent1DQExperts: {
              expertRequired: 'No'
            }
          },
          Witnesses: {
            respondent1DQWitnesses: {
              witnessesToAppear: 'No'
            }
          },
          Language: {
            respondent1DQLanguage: {
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendantSelectedCourt)
                ],
                value: listElement(config.defendantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            },
            respondent1DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          Applications: {
            respondent1DQFutureApplications: {
              intentionToMakeFutureApplications: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'NOT_FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'PART_ADMISSION'
          },

          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondToAdmittedClaimOwingAmountPounds: '2000.00'
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
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specAoSApplicantCorrespondenceAddressRequired: 'Yes',
            specAoSRespondentCorrespondenceAddressRequired: 'Yes',
            specFullDefenceOrPartAdmission: 'No',
            specDefenceFullAdmittedRequired: 'No',
            specApplicantCorrespondenceAddressRequired: 'No',
            specRespondent1Represented: 'Yes',
            respondentClaimResponseTypeForSpecGeneric: 'COUNTER_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
      case 'FULL_ADMISSION_JBA':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            responseClaimTrack: 'SMALL_CLAIM',
            specFullDefenceOrPartAdmission: 'No',
            specFullDefenceOrPartAdmission1V1: null,
          },
          defenceAdmittedPartRoute: {
            respondToAdmittedClaimOwingAmount: '200000',
            specDefenceFullAdmittedRequired: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteGeneric: 'BY_SET_DATE',
            defenceAdmitPartPaymentTimeRouteRequired: 'BY_SET_DATE',
            respondToClaimAdmitPartLRspec: {
              whenWillThisAmountBePaid:  date(40),
            }
          },
          WhyDoesNotPayImmediately: {
            responseToClaimAdmitPartWhyNotPayLRspec: 'reasons'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: 'CREATE_CLAIM_SPEC'
            },
          },
          defenceRoute: {
            specPaidLessAmountOrDisputesOrPartAdmission: 'No',
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'FULL_ADMISSION',
            specDefenceFullAdmittedRequired: 'No'
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
    }

    return responseData;
  },

  /**
   * data to respond as respondent 2.
   *
   * @param response type of response
   * @return data to respond as respondent 2.
   */
  respondToClaim2: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC', fastTrack = false) => {
    const responseData = {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSRespondent2HomeAddressRequired: 'Yes',
        },
        ResponseConfirmDetails: {
          specAoSRespondent2CorrespondenceAddressRequired: 'Yes'
        },
      },
    };

    switch (response) {
      case 'FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent2ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          defenceRoute: {
            defenceRouteRequired2: 'DISPUTES_THE_CLAIM'
          },
          Mediation: {
            responseClaimMediationSpec2Required: 'No'
          },
          ...(fastTrack ? {
            FixedRecoverableCosts: {
              respondent1DQFixedRecoverableCosts: {
                isSubjectToFixedRecoverableCostRegime: 'Yes',
                band: 'BAND_4',
                complexityBandingAgreed: 'Yes',
                reasons: 'some reasons'
              }
            },
            DisclosureOfElectronicDocumentsLRspec: {
              specRespondent1DQDisclosureOfElectronicDocuments: {
                reachedAgreement: 'No',
                agreementLikely: 'No',
                reasonForNoAgreement: 'example specific reasons'
              }
            },
            DisclosureOfNonElectronicDocumentsLRspec: {
              specRespondent1DQDisclosureOfNonElectronicDocuments: {
                bespokeDirections: 'Non electric document text'
              }
            },
          } : {}),
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent2: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, respondent 2'
            }
          },
          SmallClaimExperts: {
            respondent2DQExperts: {
              expertRequired: 'Yes',
              expertReportsSent: 'NOT_OBTAINED',
              jointExpertSuitable: 'Yes',
              details: [
                element({
                  firstName: 'John',
                  lastName: 'Doe',
                  emailAddress: 'john@doemail.com',
                  phoneNumber: '07111111111',
                  fieldOfExpertise: 'None',
                  whyRequired: 'Testing',
                  estimatedCost: '10000'
                })
              ]
            }
          },
          SmallClaimWitnesses: {
            respondent2DQWitnessesSmallClaim: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  firstName: 'Witness',
                  lastName: 'One',
                  emailAddress: 'witness@email.com',
                  phoneNumber: '07116778998',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          Language: {
            respondent2DQLanguage: {
              court: 'WELSH',
              documents: 'WELSH'
            }
          },
          SmaillClaimHearing: {
            smallClaimHearingInterpreterDescription2: 'test',
            SmallClaimHearingInterpreter2Required: 'Yes',
            respondent2DQHearingSmallClaim: {
              unavailableDatesRequired: 'No',
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendantSelectedCourt)
                ],
                value: listElement(config.defendantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            },
            respondent2DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          HearingSupport: {
            respondent2DQHearingSupport: {
              supportRequirements: 'Yes',
              supportRequirementsAdditional: 'Additional support reasons'
            }
          },
          VulnerabilityQuestions: {
            respondent2DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            respondent2DQHearing: {
              unavailableDatesRequired: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          }
        };
        break;
      case 'FULL_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent2ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceFullAdmittedRequired: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          Upload: {
            detailsOfWhyDoesYouDisputeTheClaim: 'details'
          },
          HowToAddTimeline: {
            specClaimResponseTimelineList: 'MANUAL'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          },
          defenceRoute: {
            specPaidLessAmountOrDisputesOrPartAdmission: 'No',
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'FULL_ADMISSION',
            specDefenceFullAdmittedRequired: 'No'
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent2ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceAdmittedRequired: 'No',
            respondToAdmittedClaimOwingAmount: '200000'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          FileDirectionsQuestionnaire: {
            respondent2DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'Yes',
              reactionProtocolCompliedWith: 'Yes'
            }
          },
          DisclosureOfElectronicDocumentsLRspec: {
            specRespondent2DQDisclosureOfElectronicDocuments: {
              reachedAgreement: 'Yes'
            }
          },
          Experts: {
            respondent2DQExperts: {
              expertRequired: 'No'
            }
          },
          Witnesses: {
            respondent2DQWitnesses: {
              witnessesToAppear: 'No'
            }
          },
          Language: {
            respondent2DQLanguage: {
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.defendantSelectedCourt)
                ],
                value: listElement(config.defendantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            },
            respondent1DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          Applications: {
            respondent1DQFutureApplications: {
              intentionToMakeFutureApplications: 'No'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'NOT_FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'PART_ADMISSION'
          },

          defenceAdmittedPartRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
            respondToAdmittedClaimOwingAmountPounds: '2000.00'
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
          RespondentResponseTypeSpec: {
            respondent2ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specAoSApplicantCorrespondenceAddressRequired: 'Yes',
            specAoSRespondentCorrespondenceAddressRequired: 'Yes',
            specFullDefenceOrPartAdmission: 'No',
            specDefenceFullAdmittedRequired: 'No',
            specApplicantCorrespondenceAddressRequired: 'No',
            specRespondent1Represented: 'Yes',
            respondentClaimResponseTypeForSpecGeneric: 'COUNTER_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'SMALL_CLAIM'
          }
        };
        break;

    }

    return responseData;
  }
};
