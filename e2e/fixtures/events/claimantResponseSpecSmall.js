const {listElement, element} = require('../../api/dataHelper');
const config = require('../../config.js');
module.exports = {
  claimantResponse: (hasAgreedFreeMediation = 'Yes', carmEnabled = false) => {
    const oldData = {
      userInput: {
        RespondentResponse: {
          applicant1ProceedWithClaim: 'Yes',
        },
        Mediation: {
          applicant1ClaimMediationSpecRequired: {
            hasAgreedFreeMediation: hasAgreedFreeMediation
          }
        },
        DeterminationWithoutHearing:{
          deterWithoutHearing: {
            deterWithoutHearingYesNo: 'No',
            deterWithoutHearingWhyNot: 'Incredibly valid reasons'
          }
        },
        SmallClaimExperts: {
          applicant1DQExperts: {
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
          applicant1DQWitnessesSmallClaim: {
            details: [
              element({
                firstName: 'John',
                lastName: 'Smith',
                phoneNumber: '07012345678',
                emailAddress: 'johnsmith@email.com',
                reasonForWitness: 'None'
              })
            ],
            witnessesToAppear: 'Yes'}
        },
        Language: {
          applicant1DQLanguage: {
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
        HearingSupport: {
          applicant1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Test Inc: Language Interpreter'
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
      },
      midEventData: {
        Hearing: {
          businessProcess: {
            status: 'FINISHED',
            camundaEvent: 'DEFENDANT_RESPONSE_SPEC'
          }
        }
      },
      midEventGeneratedData: {}
    };
    const newData = {
      userInput: {
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
        DeterminationWithoutHearing:{
          deterWithoutHearing: {
            deterWithoutHearingYesNo: 'No',
            deterWithoutHearingWhyNot: 'Incredibly valid reasons'
          }
        },
        SmallClaimExperts: {
          applicant1DQExperts: {
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
          applicant1DQWitnessesSmallClaim: {
            details: [
              element({
                firstName: 'John',
                lastName: 'Smith',
                phoneNumber: '07012345678',
                emailAddress: 'johnsmith@email.com',
                reasonForWitness: 'None'
              })
            ],
            witnessesToAppear: 'Yes'}
        },
        Language: {
          applicant1DQLanguage: {
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
        HearingSupport: {
          applicant1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Test Inc: Language Interpreter'
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
      },
      midEventData: {
        Hearing: {
          businessProcess: {
            status: 'FINISHED',
            camundaEvent: 'DEFENDANT_RESPONSE_SPEC'
          }
        }
      },
      midEventGeneratedData: {}
    };
    return carmEnabled ? newData : oldData;
  },
  claimantResponseRejectPartAdmit: () => {
    return {
      userInput: {
        RespondentResponse: {
          applicant1AcceptAdmitAmountPaidSpec: 'No',
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
          applicant1DQExperts: {
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
          applicant1DQWitnessesSmallClaim: {
            details: [
              element({
                firstName: 'John',
                lastName: 'Smith',
                phoneNumber: '07012345678',
                emailAddress: 'johnsmith@email.com',
                reasonForWitness: 'None'
              })
            ],
            witnessesToAppear: 'Yes'}
        },
        Language: {
          applicant1DQLanguage: {
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
        HearingSupport: {
          applicant1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Test Inc: Language Interpreter'
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
      },
    };
  },

  claimantResponsePAStatesPaid: (claimantPaymentReceived) => {
    return {
      userInput: {
        ...(claimantPaymentReceived) ? {
          RespondentResponse: {
            applicant1PartAdmitConfirmAmountPaidSpec: 'Yes',
          },
          IntentionToSettleClaim: {
            applicant1PartAdmitIntentionToSettleClaimSpec: 'No',
          }
        } : {
          RespondentResponse: {
            applicant1PartAdmitConfirmAmountPaidSpec: 'No',
          },
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
          applicant1DQExperts: {
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
          applicant1DQWitnessesSmallClaim: {
            details: [
              element({
                firstName: 'John',
                lastName: 'Smith',
                phoneNumber: '07012345678',
                emailAddress: 'johnsmith@email.com',
                reasonForWitness: 'None'
              })
            ],
            witnessesToAppear: 'Yes'}
        },
        Language: {
          applicant1DQLanguage: {
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
        HearingSupport: {
          applicant1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Test Inc: Language Interpreter'
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
      },
    };
  },
};
