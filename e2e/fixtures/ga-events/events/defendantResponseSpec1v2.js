const {listElement, date} = require('../../../api/dataHelper');
const config = require('../../config');
module.exports = {
  respondToClaim: (response = 'FULL_DEFENCE', camundaEvent = 'CREATE_CLAIM_SPEC') => {
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
            defenceRouteRequired: 'DISPUTES_THE_CLAIM',
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
            specFullDefenceOrPartAdmission: 'Yes',
            multiPartyResponseTypeFlags: 'FULL_DEFENCE',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes',
            respondent2SameLegalRepresentative: 'Yes',
            respondentResponseIsSame: 'Yes'
          },

          defenceRoute: {
            responseClaimTrack: 'SMALL_CLAIM',
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
          },
        };
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'FULL_DEFENCE'
          },
          defenceRoute: {
            defenceRouteRequired: 'DISPUTES_THE_CLAIM',
          },

          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
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
            defenceAdmitPartPaymentTimeRouteRequired: 'SUGGESTION_OF_REPAYMENT_PLAN'
          },
          DisabilityPremiumPayments: {
            disabilityPremiumPayments: 'Yes',
            severeDisabilityPremiumPayments: 'Yes',
          },
          defendantHomeOptions: {
            respondent1DQHomeDetails: {
              type: 'PRIVATE_RENTAL'
            }
          },
          DefendantPartnersAndDependents: {
            respondent1PartnerAndDependent: {
              haveAnyChildrenRequired: 'Yes',
              howManyChildrenByAgeGroup: {
                numberOfUnderEleven: '1',
                numberOfElevenToFifteen: '1',
                numberOfSixteenToNineteen: '0'
              },
              liveWithPartnerRequired: 'Yes',
              partnerAgedOver: 'Yes',
              receiveDisabilityPayments: 'Yes',
              supportPeopleDetails: 'details',
              supportPeopleNumber: '2',
              supportedAnyoneFinancialRequired: 'Yes'
            }
          },
          EmploymentDeclaration:{
            defenceAdmitPartEmploymentTypeRequired: 'No',
            respondToClaimAdmitPartUnemployedLRspec: {
              unemployedComplexTypeRequired: 'RETIRED'
            }
          },
          DetailsOfPayingMoneyRepaymentPlan: {
            respondent1CourtOrderPaymentOption: 'No'
          },
          DefendantDebts: {
            respondent1LoanCreditOption: 'No'
          },
          DefendantIncomeExpensesFullAdmission: {
            respondent1DQCarerAllowanceCreditFullAdmission: 'No',
            respondent1DQRecurringExpensesFA: [],
            respondent1DQRecurringIncomeFA: []
          },
          WhyDoesNotPayImmediately: {
            responseToClaimAdmitPartWhyNotPayLRspec: 'reasons'
          },
          WhyDoesNotPayImmediatelyRespondent2: {
            responseToClaimAdmitPartWhyNotPayLRspec2: 'reasons 2'
          },
          RepaymentPlan: {
            respondent1RepaymentPlan: {
              firstRepaymentDate: date(30),
              paymentAmount: '100',
              repaymentFrequency: 'ONCE_ONE_MONTH'
            }
          },
          RepaymentPlanRespondent2: {
            respondent2RepaymentPlan: {
              firstRepaymentDate: date(60),
              paymentAmount: '200',
              repaymentFrequency: 'ONCE_TWO_WEEKS'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'name',
              role: 'role'
            }
          },

          ResponseConfirmNameAddress: {
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: camundaEvent
            },
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
            respondentResponseIsSame: 'Yes',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes',
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
          SingleResponse: {
            respondentResponseIsSame: 'Yes'
          },
          RespondentResponseTypeSpec: {
            respondent1ClaimResponseTypeForSpec: 'PART_ADMISSION'
          },
          defenceAdmittedPartRoute: {
            specDefenceAdmittedRequired: 'No',
            respondToAdmittedClaimOwingAmount: '1000'
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
            responseClaimTrack: 'SMALL_CLAIM',
            respondToAdmittedClaimOwingAmountPounds: '10.00'
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
            defenceRouteRequired: 'DISPUTES_THE_CLAIM',
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
          ResponseConfirmDetails: {
            sameSolicitorSameResponse: 'Yes'
          },

          RespondentResponseTypeSpec: {
            specFullDefenceOrPartAdmission: 'Yes',
            // this value changed on 2015
            // multiPartyResponseTypeFlags: 'COUNTER_ADMIT_OR_ADMIT_PART',
            specDefenceFullAdmittedRequired: 'No',
            respondentClaimResponseTypeForSpecGeneric: 'FULL_DEFENCE',
            specRespondent1Represented: 'Yes',
            specRespondent2Represented: 'Yes',
            respondent2SameLegalRepresentative: 'Yes',
            sameSolicitorSameResponse: 'No'
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
          }
        };
        break;
    }

    return responseData;
  }
};
