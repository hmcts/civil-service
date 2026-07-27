const {listElement, element} = require('../../api/dataHelper');
const config = require('../../config.js');
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
          FixedRecoverableCosts: {
            applicant1DQFixedRecoverableCosts: {
              isSubjectToFixedRecoverableCostRegime: 'Yes',
              band: 'BAND_4',
              complexityBandingAgreed: 'Yes',
              reasons: 'some reasons'
            }
          },
          FileDirectionsQuestionnaire: {
            applicant1DQFileDirectionsQuestionnaire: {
              explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'No',
              reactionProtocolCompliedWith: 'No',
              reactionProtocolNotCompliedWithReason: 'test'
            }
          },
          DisclosureOfElectronicDocuments: {
            applicant1DQDisclosureOfElectronicDocuments: {
              reachedAgreement: 'No',
              agreementLikely: 'No',
              reasonForNoAgreement: 'issues'
            }
          },
          DisclosureOfNonElectronicDocuments: {
            specApplicant1DQDisclosureOfNonElectronicDocuments: {
              bespokeDirections: 'directions'
            }
          },
          DisclosureReport: {
            applicant1DQDisclosureReport: {
              disclosureFormFiledAndServed: 'Yes',
              disclosureProposalAgreed: 'Yes',
              draftOrderNumber: '123'
            }
          },
          Experts: {
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
          Witnesses: {
            applicant1DQWitnesses: {
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
            applicant1DQHearingLRspec: {
              hearingLength: 'ONE_DAY',
              unavailableDatesRequired: 'No',
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
            },
            applicant1DQRemoteHearingLRspec: {
              remoteHearingRequested: 'Yes',
              reasonForRemoteHearing: 'Some reason'
            }
          },
          HearingSupport: {
            applicant1DQHearingSupport: {
              supportRequirements: 'Yes',
              supportRequirementsAdditional: 'Additional support reasons'
            }
          },
          VulnerabilityQuestions: {
            applicant1DQVulnerabilityQuestions: {
              vulnerabilityAdjustmentsRequired: 'No'
            }
          },
          Applications: {
            applicant1DQFutureApplications: {
              intentionToMakeFutureApplications: 'No'
            }
          },
          StatementOfTruth: {
            uiStatementOfTruth: {
              name: 'Solicitor name',
              role: 'Solicitor role'
            },
            applicant1DQHearing: {
              hearingLength: 'ONE_DAY'
            }
          }
        };
        responseData.midEventData = {
          ...responseData.midEventData,
          Experts: {
            respondent1DQHearing: {
              hearingLength: 'ONE_DAY'
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

