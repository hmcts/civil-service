const {listElement, element} = require('../../api/dataHelper');
const config = require('../../config.js');

let defendantCourt = config.claimantSelectedCourt;
const useHmcEaCourt = config.claimantSelectedCourtHmc;
const useDefendantSelectedCourt = config.claimantSelectedCourt;

module.exports = {
  respondToClaim: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC', hmcTest) => {
    defendantCourt = hmcTest ? useHmcEaCourt : useDefendantSelectedCourt;

    const responseData = {
      userInput: {
        ResponseConfirmNameAddress: {
          specAoSApplicantCorrespondenceAddressRequired: 'Yes',
          specAoSRespondent2HomeAddressRequired: 'Yes'
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
          SingleResponse: {
            respondentResponseIsSame: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
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
          FileDirectionsQuestionnaire: {
            respondent1DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'No',
              reactionProtocolCompliedWith: 'No',
              reactionProtocolNotCompliedWithReason: 'reason'
            }
          },
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
              agreementLikely: 'No',
              reachedAgreement: 'No',
              reasonForNoAgreement: 'issue'
            }
          },
          DisclosureOfNonElectronicDocumentsLRspec: {
            specRespondent1DQDisclosureOfNonElectronicDocuments: {
              bespokeDirections: 'directions'
            }
          },
          DisclosureReport: {
            respondent1DQDisclosureReport: {
              disclosureFormFiledAndServed: 'Yes',
              disclosureProposalAgreed: 'Yes',
              draftOrderNumber: '123'
            }
          },
          Experts: {
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
          Witnesses: {
            respondent1DQWitnesses: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  firstName: 'John',
                  lastName: 'Smith',
                  phoneNumber: '07012345678',
                  emailAddress: 'johnsmith@email.com',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          Language: {
            respondent1DQLanguage: {
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          HearingLRspec: {
            respondent1DQHearing: {
              hearingLength: 'ONE_DAY',
              unavailableDatesRequired: 'No'
            }
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(defendantCourt)
                ],
                value: listElement(defendantCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
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
          Applications:{
            additionalInformationForJudge: 'information',
            respondent1DQFutureApplications: {
              intentionToMakeFutureApplications: 'No'
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
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE',
            respondent2SameLegalRepresentative: 'Yes',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes'
          },

          defenceRoute: {
            responseClaimTrack: 'FAST_CLAIM',
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY'
          },

          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
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
          SingleResponse: {
            respondentResponseIsSame: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceFullAdmittedRequired: 'No'
          },
          WhenWillClaimBePaid: {
            defenceAdmitPartPaymentTimeRouteRequired: 'IMMEDIATELY'
          },
          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(defendantCourt)
                ],
                value: listElement(defendantCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            },
            respondent1DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          }, 
          defenceRoute: {
            specPaidLessAmountOrDisputesOrPartAdmission: 'No',
          },
          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'No',
            multiPartyResponseTypeFlags: 'NOT_FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_ADMISSION',
          },
          defenceAdmittedPartRoute: {
            responseClaimTrack: 'FAST_CLAIM'
          },
          defenceRoute: {
            respondent1ClaimResponsePaymentAdmissionForSpec: 'DID_NOT_PAY',
            responseClaimTrack: 'FAST_CLAIM'
          }
        };
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse: {
            respondentResponseIsSame: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
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
          FileDirectionsQuestionnaire: {
            respondent1DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'Yes',
              reactionProtocolCompliedWith: 'Yes'
            }
          },
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
          HearingLRspec: {
            respondent1DQHearing: {
              hearingLength: 'ONE_DAY',
              unavailableDatesRequired: 'No'
            },
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(defendantCourt)
                ],
                value: listElement(defendantCourt)
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
          },
          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
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
            responseClaimTrack: 'FAST_CLAIM',
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
            responseClaimTrack: 'FAST_CLAIM'
          }
        };
        break;
      case 'COUNTER_CLAIM':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse: {
            respondentResponseIsSame: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,

          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
          },

          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            respondentClaimResponseTypeForSpecGeneric: 'COUNTER_CLAIM',
            sameSolicitorSameResponse: 'Yes',
            specDefenceFullAdmittedRequired: 'No',
            specFullDefenceOrPartAdmission: 'No',
            respondent2SameLegalRepresentative: 'Yes',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },
          FixedRecoverableCosts: {
            respondent1DQFixedRecoverableCosts: {
              isSubjectToFixedRecoverableCostRegime: 'Yes',
              band: 'BAND_4',
              complexityBandingAgreed: 'Yes',
              reasons: 'some reasons'
            }
          }
        };
        break;

      case 'DIFF_FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          SingleResponse: {
            respondentResponseIsSame: 'No'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            respondent2ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
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
          FileDirectionsQuestionnaire: {
            respondent1DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'No',
              reactionProtocolCompliedWith: 'No',
              reactionProtocolNotCompliedWithReason: 'reason'
            }
          },
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
              agreementLikely: 'No',
              reachedAgreement: 'No',
              reasonForNoAgreement: 'issue'
            }
          },
          DisclosureOfNonElectronicDocumentsLRspec: {
            specRespondent1DQDisclosureOfNonElectronicDocuments: {
              bespokeDirections: 'directions'
            }
          },
          DisclosureReport: {
            respondent1DQDisclosureReport: {
              disclosureFormFiledAndServed: 'Yes',
              disclosureProposalAgreed: 'Yes',
              draftOrderNumber: '123'
            }
          },
          Experts: {
            respondent1DQExperts: {
              expertRequired: 'No'
            },
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
          HearingLRspec: {
            respondent1DQHearing: {
              hearingLength: 'ONE_DAY',
              unavailableDatesRequired: 'No'
            }
          },
          RequestedCourtLocationLRspec: {
            respondToCourtLocation: {
              responseCourtLocations: {
                list_items: [
                  listElement(defendantCourt)
                ],
                value: listElement(defendantCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
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
          Applications:{
            additionalInformationForJudge: 'information',
            respondent1DQFutureApplications: {
              intentionToMakeFutureApplications: 'No'
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
          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
          },

          RespondentResponseTypeSpec: {
            // this value changed on 2015
            // multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE',
            sameSolicitorSameResponse: 'No',
            specDefenceFullAdmittedRequired: 'No',
            specFullDefenceOrPartAdmission: 'Yes',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes',
            respondent2SameLegalRepresentative: 'Yes'
          },

          defenceRoute: {
            responseClaimTrack: 'FAST_CLAIM',
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
          SingleResponse: {
            respondentResponseIsSame: 'No'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_ADMISSION',
            respondent2ClaimResponseTypeForSpec: 'COUNTER_CLAIM'
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
          },

          RespondentResponseTypeSpec: {
            multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            sameSolicitorSameResponse: 'No',
            specDefenceFullAdmittedRequired: 'No',
            specFullDefenceOrPartAdmission: 'No',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes',
            respondent2SameLegalRepresentative: 'Yes'
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            }
          },
          FixedRecoverableCosts: {
            respondent2DQFixedRecoverableCosts: {
              isSubjectToFixedRecoverableCostRegime: 'Yes',
              band: 'BAND_4',
              complexityBandingAgreed: 'Yes',
              reasons: 'some reasons'
            }
          },
        };
        break;

    }

    return responseData;
  }
};
