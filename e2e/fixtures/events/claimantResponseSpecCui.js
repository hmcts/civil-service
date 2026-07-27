const {listElement} = require('../../api/dataHelper');
const config = require('../../config.js');
module.exports = {
  claimantResponse: (response = 'FULL_DEFENCE', citizenDefendantResponse = false, freeMediation = 'Yes', carmEnabled = false) => {
    const responseData = {
    };
    switch (response) {
      case 'FULL_DEFENCE':
        carmEnabled ? responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1ProceedWithClaim: 'Yes',
          },
          MediationContactInformation:{
            app1MediationContactInfo: {
              firstName:'Jane',
              lastName: 'Smith',
              emailAddress:'jane.smith@doemail.com',
              telephoneNumber:'07111111111'
            }
          },
          MediationAvailability: {
            app1MediationAvailability: {
              isMediationUnavailablityExists: 'No'
            }
          },
          SmallClaimExperts: {
            applicant1ClaimExpertSpecRequired: 'No'
          },
          SmallClaimWitnesses: {
            applicant1ClaimWitnesses: '10'
          },
          Language: {
            applicant1DQLanguage: {
              evidence: 'ENGLISH',
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          Hearing: {
            applicant1DQSmallClaimHearing: {
              unavailableDatesRequired: 'No'
            }
          },
          ApplicantCourtLocationLRspec: {
            applicant1DQRequestedCourt: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.claimantSelectedCourt)
                ],
                value: listElement(config.claimantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            }
          },
          VulnerabilityQuestions: {
            applicant1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'No'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Solicitor name',
              role: 'Solicitor role'
            }
          }
        } : responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1ProceedWithClaim: 'Yes',
          },
          Mediation: {
            applicant1ClaimMediationSpecRequiredLip: {
              hasAgreedFreeMediation: freeMediation
            }
          },
          SmallClaimExperts: {
            applicant1ClaimExpertSpecRequired: 'No'
          },
          SmallClaimWitnesses: {
            applicant1ClaimWitnesses: '10'
          },
          Language: {
            applicant1DQLanguage: {
              evidence: 'ENGLISH',
              court: 'ENGLISH',
              documents: 'ENGLISH'
            }
          },
          Hearing: {
            applicant1DQSmallClaimHearing: {
              unavailableDatesRequired: 'No'
            }
          },
          ApplicantCourtLocationLRspec: {
            applicant1DQRequestedCourt: {
              responseCourtLocations: {
                list_items: [
                  listElement(config.claimantSelectedCourt)
                ],
                value: listElement(config.claimantSelectedCourt)
              },
              reasonForHearingAtSpecificCourt: 'Reasons'
            }
          },
          VulnerabilityQuestions: {
            applicant1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'No'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Solicitor name',
              role: 'Solicitor role'
            }
          }
        };
        if (citizenDefendantResponse) {
          responseData.midEventData = {
            ...responseData.midEventData,
            Hearing: {
              businessProcess: {
                status: 'FINISHED',
                camundaEvent: 'DEFENDANT_RESPONSE_CUI'
              }
            }
          };
        } else {
          responseData.midEventData = {
            ...responseData.midEventData,
            Hearing: {
              businessProcess: {
                status: 'FINISHED',
                camundaEvent: 'DEFENDANT_RESPONSE_SPEC'
              }
            }
          };
        }
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1AcceptAdmitAmountPaidSpec: 'Yes',
            applicant1AcceptPartAdmitPaymentPlanSpec: 'Yes'
          },
          CcjPaymentPaidSome: {
            ccjPaymentPaidSomeOption: 'Yes',
            ccjPaymentPaidSomeAmount: '1000',
          },
          FixedCost: {
            ccjJudgmentFixedCostOption: 'Yes',
          },
          CcjJudgmentSummary: {
            ccjJudgmentAmountClaimAmount: '1000',
            ccjJudgmentAmountInterestToDate: '35',
            ccjJudgmentAmountClaimFee: '100',
            ccjJudgmentFixedCostAmount: '40',
            ccjJudgmentAmountSubtotal: '1175',
            ccjPaymentPaidSomeAmountInPounds: '10',
            ccjJudgmentTotalStillOwed: '1165',
            ccjJudgmentStatement: 'test'
          },
          Mediation: {
            applicant1ClaimMediationSpecRequiredLip: {
              hasAgreedFreeMediation: freeMediation
            }
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;

      case 'PART_ADMISSION_SETTLE':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1PartAdmitConfirmAmountPaidSpec: 'Yes',
          },
          IntentionToSettleClaim: {
            applicant1PartAdmitIntentionToSettleClaimSpec: 'Yes',
          },
          Mediation: {
            applicant1ClaimMediationSpecRequiredLip: {
              hasAgreedFreeMediation: freeMediation
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;

      case 'FULL_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1AcceptFullAdmitPaymentPlanSpec: 'Yes',
          },
          RespondentProposedRepayment: {
            applicant1RepaymentOptionForDefendantSpec: 'SET_DATE',
          },
          PaymentDate: {
            applicant1RequestedPaymentDateForDefendantSpec : {
              paymentSetDate: '2220-01-01'
            }
          },
          SuggestInstalments: {
            applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec : '2220-01-01',
            applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec: 'ONCE_ONE_WEEK',
            applicant1SuggestInstalmentsPaymentAmountForDefendantSpec: '3'
          },
          Mediation: {
            applicant1ClaimMediationSpecRequiredLip: {
              hasAgreedFreeMediation: freeMediation
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;

      case 'NOT_PROCEED':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1ProceedWithClaim: 'No',
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;
    }
    return responseData;
  }
};
