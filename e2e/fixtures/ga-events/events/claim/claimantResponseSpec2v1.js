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
            applicant1ProceedWithClaimSpec2v1: 'Yes',
          },
          Mediation: {
            applicantMPClaimMediationSpecRequired: {
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
            claimant1ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            claimant2ClaimResponseTypeForSpec: 'FULL_DEFENCE',
            defendantSingleResponseToBothClaimants: 'Yes',
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
            applicant1ProceedWithClaimSpec2v1: 'Yes',
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
            applicant1ProceedWithClaimSpec2v1: 'Yes',
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
            applicant1ProceedWithClaimSpec2v1: 'No',
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


