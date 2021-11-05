const {date, element} = require('../../api/dataHelper');

module.exports = {
  valid: {
    RespondentResponse: {
      applicant1ProceedWithClaim: 'Yes'
    },
    ApplicantDefenceResponseDocument: {
      applicant1DefenceResponseDocument: {
        file: {
          document_url: "${TEST_DOCUMENT_URL}",
          document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
          document_filename: "${TEST_DOCUMENT_FILENAME}"
        }
      }
    },
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
        standardDirectionsRequired: 'Yes',
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
            name: 'John Doe',
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
            name: 'John Doe',
            reasonForWitness: 'None'
          })
        ]
      }
    },
    Hearing: {
      applicant1DQHearing: {
        hearingLength: 'MORE_THAN_DAY',
        hearingLengthDays: '5',
        unavailableDatesRequired: 'Yes',
        unavailableDates: [
          element({
            date: date(10),
            who: 'Foo Bar'
          })
        ]
      }
    },
    DraftDirections: {
      applicant1DQDraftDirections: {
        document_url: "${TEST_DOCUMENT_URL}",
        document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
        document_filename: "${TEST_DOCUMENT_FILENAME}"
      }
    },
    HearingSupport: {},
    FurtherInformation: {
      applicant1DQFurtherInformation: {
        futureApplications: 'Yes',
        otherInformationForJudge: 'Nope',
        reasonForFutureApplications: 'Nothing'
      }
    },
    Language: {
      applicant1DQLanguage: {
        evidence: 'WELSH',
        court: 'WELSH',
        documents: 'WELSH'
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
          hearingLength: 'MORE_THAN_DAY',
          hearingLengthDays: 5,
          unavailableDatesRequired: 'Yes',
          unavailableDates: [
            element({
              date: date(-1),
              who: 'Foo Bar'
            })
          ]
        }
      },
      moreThanYear: {
        applicant1DQHearing: {
          hearingLength: 'MORE_THAN_DAY',
          hearingLengthDays: 5,
          unavailableDatesRequired: 'Yes',
          unavailableDates: [
            element({
              date: date(367),
              who: 'Foo Bar'
            })
          ]
        }
      }
    }
  }
};
