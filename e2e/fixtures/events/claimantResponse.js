const {date, element} = require('../../api/dataHelper');

const createRespondentResponseAndApplicantDefenceResponseDocument = (mpScenario) => {
  switch (mpScenario){
    case 'ONE_V_TWO_ONE_LEGAL_REP': {
      return {
        RespondentResponse: {
          applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2: 'Yes',
          applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2: 'Yes',
          claimant2ResponseFlag: 'No',
          applicantsProceedIntention: 'Yes',
          claimantResponseDocumentToDefendant2Flag: 'No'

        },
        ApplicantDefenceResponseDocument: {
          applicant1DefenceResponseDocument: {
            file: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        }
      };
    }
    case 'ONE_V_TWO_TWO_LEGAL_REP': {
      return {
        RespondentResponse: {
          applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2: 'Yes',
          applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2: 'Yes',
          claimant2ResponseFlag: 'No',
          applicantsProceedIntention: 'Yes',
          claimantResponseDocumentToDefendant2Flag: 'Yes'
        },
        ApplicantDefenceResponseDocument: {
          applicant1DefenceResponseDocument: {
            file: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        }
      };
    }
    case 'TWO_V_ONE': {
      return {
        RespondentResponse: {
          applicant1ProceedWithClaimMultiParty2v1: 'Yes',
          applicant2ProceedWithClaimMultiParty2v1: 'Yes',
          claimant2ResponseFlag: 'No',
          applicantsProceedIntention: 'Yes',
          claimantResponseDocumentToDefendant2Flag: 'No'

        },
        ApplicantDefenceResponseDocument: {
          applicant1DefenceResponseDocument: {
            file: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        }
      };
    }
    case 'ONE_V_ONE':
    default: {
      return {
        RespondentResponse: {
          applicant1ProceedWithClaim: 'Yes',
          claimant2ResponseFlag: 'No',
          applicantsProceedIntention: 'Yes',
          claimantResponseDocumentToDefendant2Flag: 'No'

        },
        ApplicantDefenceResponseDocument: {
          applicant1DefenceResponseDocument: {
            file: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        }
      };
    }

  }
};

module.exports = {
  claimantResponse: (mpScenario = 'ONE_V_ONE', allocatedTrack = 'SMALL_CLAIM') => {
    return {
      valid: {
        ...createRespondentResponseAndApplicantDefenceResponseDocument(mpScenario),
        FileDirectionsQuestionnaire: {
          applicant1DQFileDirectionsQuestionnaire: {
            explainedToClient: ['CONFIRM'],
              oneMonthStayRequested: 'Yes',
              reactionProtocolCompliedWith: 'Yes'
          }
        },
        ...(allocatedTrack === 'FAST_CLAIM' || allocatedTrack === 'MULTI_CLAIM'? {
          FixedRecoverableCosts: {
            applicant1DQFixedRecoverableCosts: {
              band: 'BAND_1',
              reasons: 'reasons',
              complexityBandingAgreed: 'Yes',
              isSubjectToFixedRecoverableCostRegime: 'Yes'
            }
          }
        } : {}),
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' ? {
          FixedRecoverableCosts: {
            applicant1DQFixedRecoverableCostsIntermediate: {
              band: 'BAND_1',
              reasons: 'reasons',
              complexityBandingAgreed: 'Yes',
              isSubjectToFixedRecoverableCostRegime: 'Yes',
              frcSupportingDocument: {
                document_url: '${TEST_DOCUMENT_URL}',
                document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                document_filename: '${TEST_DOCUMENT_FILENAME}'
              }
            }
          }
        } : {}),
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' || allocatedTrack === 'MULTI_CLAIM' ? {
        DisclosureOfElectronicDocuments: {
          applicant1DQDisclosureOfElectronicDocuments: {
            reachedAgreement: 'No',
              agreementLikely: 'Yes'
            },
            ...(mpScenario === 'TWO_V_ONE' ? {
              applicant2DQDisclosureOfElectronicDocuments: {
                reachedAgreement: 'No',
                agreementLikely: 'Yes'
        },
            } : {})
          }
        } : {}),
        DisclosureOfNonElectronicDocuments: {
          applicant1DQDisclosureOfNonElectronicDocuments: {
            directionsForDisclosureProposed: 'Yes',
              standardDirectionsRequired: 'No',
              bespokeDirections: 'directions'
          },
          ...(mpScenario === 'TWO_V_ONE' ? {
            applicant2DQDisclosureOfNonElectronicDocuments: {
              directionsForDisclosureProposed: 'Yes',
              standardDirectionsRequired: 'No',
              bespokeDirections: 'directions'
            },
          } : {})
        },
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' || allocatedTrack === 'MULTI_CLAIM' ? {
          DisclosureReport: {
            applicant1DQDisclosureReport:
              {
                disclosureFormFiledAndServed: 'Yes',
                disclosureProposalAgreed: 'Yes',
                draftOrderNumber: '012345'
              },
            ...(mpScenario === 'TWO_V_ONE' ? {
              applicant2DQDisclosureReport:
                {
                  disclosureFormFiledAndServed: 'Yes',
                  disclosureProposalAgreed: 'Yes',
                  draftOrderNumber: '012345'
          }
            } : {})
          }
        } : {}),
        ...(allocatedTrack === 'SMALL_CLAIM' ? {
          DeterminationWithoutHearing:{
            deterWithoutHearing: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, applicant'
            }
          },
        } : {}),
        Experts: {
          applicant1DQExperts: {
            expertRequired: 'Yes',
              expertReportsSent: 'NOT_OBTAINED',
              jointExpertSuitable: 'Yes',
              details: [
              element({
                firstName: 'John',
                lastName: 'Doe',
                emailAddress: 'test@email.com',
                phoneNumber: '07000111000',
                fieldOfExpertise: 'None',
                whyRequired: 'Testing',
                estimatedCost: '10000'
              })
            ]
          }
        },
        Witnesses: {
          applicant1DQWitnesses: {
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
          applicant1DQLanguage: {
              court: 'WELSH',
              documents: 'WELSH'
          }
        },
        Hearing: {
          applicant1DQHearing: {
            unavailableDatesRequired: 'Yes',
            unavailableDates: [
              element({
                unavailableDateType: 'SINGLE_DATE',
                date: date(10)
              }),
              element({
                fromDate: date(30),
                toDate: date(35),
                unavailableDateType: 'DATE_RANGE',
              })
            ]
          }
        },
        DraftDirections: {
          applicant1DQDraftDirections: {
            document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
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
            vulnerabilityAdjustments: 'claimant reasons'
          }
        },
        FurtherInformation: {
          applicant1DQFurtherInformation: {
            futureApplications: 'Yes',
              otherInformationForJudge: 'Nope',
              reasonForFutureApplications: 'Nothing'
          }
        },

        StatementOfTruth: {
          uiStatementOfTruth: {
            name: 'James Bond',
              role: 'Spy'
          }
        }
      },
      midEventData: {
        // otherwise applicantSolicitor1ClaimStatementOfTruth: [undefined]
        StatementOfTruth: {
          applicantSolicitor1ClaimStatementOfTruth: {}
        }
      },
      invalid: {
        Experts: {
          emptyDetails: {
            applicant1DQExperts: {
              details: [],
                expertRequired: 'Yes',
                expertReportsSent: 'NOT_OBTAINED',
                jointExpertSuitable: 'Yes'
            }
          }
        },
        Hearing: {
          past: {
            applicant1DQHearing: {
                unavailableDatesRequired: 'Yes',
                unavailableDates: [
                element({
                  date: date(-1),
                  unavailableDateType: 'SINGLE_DATE',
                })
              ]
            }
          },
          moreThanYear: {
            applicant1DQHearing: {
                unavailableDatesRequired: 'Yes',
                unavailableDates: [
                element({
                  date: date(367),
                  unavailableDateType: 'SINGLE_DATE',
                })
              ]
            }
          },
          wrongDateRange: {
            applicant1DQHearing: {
              unavailableDatesRequired: 'Yes',
              unavailableDates: [
                element({
                  fromDate: date(15),
                  toDate: date(10),
                  unavailableDateType: 'DATE_RANGE',
                })
              ]
            }
          }
        }
      }
    };
  }
};
