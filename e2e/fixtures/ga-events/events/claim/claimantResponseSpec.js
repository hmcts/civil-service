const {listElement} = require('../../../../api/dataHelper');
const config = require('../../../../config.js');
module.exports = {
  claimantResponse: (response = 'FULL_DEFENCE') => {
    const responseData = {
    };
    switch (response) {
      case 'FULL_DEFENCE':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1ProceedWithClaim: 'Yes',
          },
          Mediation: {
            applicant1ClaimMediationSpecRequired: {
              hasAgreedFreeMediation: 'Yes'
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
        responseData.midEventData = {
          ...responseData.midEventData,
          Hearing: {
            respondent1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
            },
            respondent1DQStatementOfTruth: {
              name: 'Test',
              role: 'Worker'
            },
            businessProcess: {
              status: 'FINISHED',
              camundaEvent: 'DEFENDANT_RESPONSE_SPEC'
            }
          }
        };
        break;
      case 'PART_ADMISSION':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1ProceedWithClaim: 'Yes',
          },
          Mediation: {
            applicantMPClaimMediationSpecRequired: {
              hasAgreedFreeMediation: 'Yes'
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
            applicant1ProceedWithClaim: 'Yes',
          },
          Mediation: {
            applicantMPClaimMediationSpecRequired: {
              hasAgreedFreeMediation: 'Yes'
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

      case 'PART_ADMISSION_IMMEDIATELY':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1AcceptAdmitAmountPaidSpec: 'Yes',
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;
    }
    return responseData;
  }
};
