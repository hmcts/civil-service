const {listElement, element} = require('../../api/dataHelper');
const config = require('../../config.js');
module.exports = {
  claimantResponse: (response = 'FULL_DEFENCE', fastTrack = false, carmEnabled = false) => {
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
            ...(fastTrack ? {
              FixedRecoverableCosts: {
                applicant1DQFixedRecoverableCosts: {
                  isSubjectToFixedRecoverableCostRegime: 'Yes',
                  band: 'BAND_4',
                  complexityBandingAgreed: 'Yes',
                  reasons: 'some reasons'
                }
              }
            } : {}),
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
                court: 'WELSH',
                documents: 'WELSH'
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
                vulnerabilityAdjustmentsRequired: 'Yes',
                vulnerabilityAdjustments: 'test'
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
            applicant1ClaimMediationSpecRequired: {
              hasAgreedFreeMediation: 'Yes'
            }
          },
          ...(fastTrack ? {
            FixedRecoverableCosts: {
              applicant1DQFixedRecoverableCosts: {
                isSubjectToFixedRecoverableCostRegime: 'Yes',
                band: 'BAND_4',
                complexityBandingAgreed: 'Yes',
                reasons: 'some reasons'
              }
            }
          } : {}),
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
              court: 'WELSH',
              documents: 'WELSH'
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
              vulnerabilityAdjustmentsRequired: 'Yes',
              vulnerabilityAdjustments: 'test'
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
    }
    return responseData;
  }
};
