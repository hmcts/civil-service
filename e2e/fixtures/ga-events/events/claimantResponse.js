const {date, element} = require('../../../api/dataHelper');

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
  claimantResponse: (mpScenario = 'ONE_V_ONE') => {
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
        DisclosureOfElectronicDocuments: {
          applicant1DQDisclosureOfElectronicDocuments: {
            reachedAgreement: 'No',
            agreementLikely: 'Yes'
          }
        },
        DisclosureOfNonElectronicDocuments: {
          applicant1DQDisclosureOfNonElectronicDocuments: {
            directionsForDisclosureProposed: 'Yes',
            standardDirectionsRequired: 'No',
            bespokeDirections: 'directions'
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
