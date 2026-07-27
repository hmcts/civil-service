const {date, element, buildAddress} = require('../../../../api/dataHelper');

module.exports = {
  valid: {
    ConfirmDetails: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        individualDateOfBirth: date(-1),
        primaryAddress: buildAddress('respondent'),
        partyName: 'Sir John Doe',
        partyTypeDisplayValue: 'Individual',
      },
      respondent2: {
        type: 'INDIVIDUAL',
        individualFirstName: 'Foo',
        individualLastName: 'Bar',
        individualTitle: 'Dr',
        primaryAddress: buildAddress('second respondent'),
        individualDateOfBirth: date(-1),
        partyName: 'Dr Foo Bar',
        partyTypeDisplayValue: 'Individual',
      }
    },
    SingleResponse: {
      respondentResponseIsSame: 'Yes',
    },
    RespondentResponseType: {
      respondent1ClaimResponseType: 'FULL_DEFENCE',
      multiPartyResponseTypeFlags: 'FULL_DEFENCE'
    },
    Upload: {
      respondent1ClaimResponseDocument: {
        file: {
          document_url: '${TEST_DOCUMENT_URL}',
          document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
          document_filename: '${TEST_DOCUMENT_FILENAME}'
        }
      }
    },
    FileDirectionsQuestionnaire: {
      respondent1DQFileDirectionsQuestionnaire: {
        explainedToClient: ['CONFIRM'],
        oneMonthStayRequested: 'Yes',
        reactionProtocolCompliedWith: 'Yes'
      }
    },
    DisclosureOfElectronicDocuments: {
      respondent1DQDisclosureOfElectronicDocuments: {
        reachedAgreement: 'No',
        agreementLikely: 'Yes'
      }
    },
    DisclosureOfNonElectronicDocuments: {
      respondent1DQDisclosureOfNonElectronicDocuments: {
        directionsForDisclosureProposed: 'Yes',
        standardDirectionsRequired: 'Yes',
        bespokeDirections: 'directions'
      }
    },
    Experts: {
      respondent1DQExperts: {
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
      respondent1DQWitnesses: {
        witnessesToAppear: 'Yes',
        details: [
          element({
            name: 'John Doe',
            reasonForWitness: 'None'
          })
        ]
      }
    },
    Language: {
      respondent1DQLanguage: {
        evidence: 'WELSH',
        court: 'WELSH',
        documents: 'WELSH'
      }
    },
    Hearing: {
      respondent1DQHearing: {
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
      respondent1DQDraftDirections: {
        document_url: '${TEST_DOCUMENT_URL}',
        document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
        document_filename: '${TEST_DOCUMENT_FILENAME}'
      }
    },
    RequestedCourt: {
      respondent1DQRequestedCourt: {
        responseCourtCode: '343',
        reasonForHearingAtSpecificCourt: 'No reasons',
        requestHearingAtSpecificCourt: 'Yes'
      }
    },
    HearingSupport: {},
    FurtherInformation: {
      respondent1DQFurtherInformation: {
        futureApplications: 'Yes',
        otherInformationForJudge: 'Nope',
        reasonForFutureApplications: 'Nothing'
      }
    },
    StatementOfTruth: {
      uiStatementOfTruth: {
        name: 'John Doe',
        role: 'Tester'
      }
    }
  },
  midEventData: {
    // otherwise applicantSolicitor1ClaimStatementOfTruth: [undefined]
    StatementOfTruth: {
      applicantSolicitor1ClaimStatementOfTruth: {}
    },
  },
  invalid: {
    ConfirmDetails: {
      futureDateOfBirth: {
        respondent1: {
          type: 'INDIVIDUAL',
          individualFirstName: 'John',
          individualLastName: 'Doe',
          individualTitle: 'Sir',
          individualDateOfBirth: date(1),
          primaryAddress: buildAddress('respondent')
        }
      }
    },
    Experts: {
      emptyDetails: {
        respondent1DQExperts: {
          details: [],
          expertRequired: 'Yes',
          expertReportsSent: 'NOT_OBTAINED',
          jointExpertSuitable: 'Yes'
        }
      }
    },
    Hearing: {
      past: {
        respondent1DQHearing: {
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
        respondent1DQHearing: {
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
    },
  }
};
