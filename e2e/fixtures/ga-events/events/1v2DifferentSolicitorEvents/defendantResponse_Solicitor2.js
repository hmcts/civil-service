const {date, element, buildAddress, listElement} = require('../../../../api/dataHelper');
const config = require('../../../../config');

module.exports = {
  valid: {
    ConfirmDetails: {
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
    RespondentResponseType: {
      respondent2ClaimResponseType: 'FULL_DEFENCE',
      multiPartyResponseTypeFlags: 'FULL_DEFENCE'
    },
    SolicitorReferences: {
      solicitorReferences:{
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      },
      solicitorReferencesCopy:{
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      }
    },
    Upload: {
      respondent2ClaimResponseDocument: {
        file: {
          document_url: '${TEST_DOCUMENT_URL}',
          document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
          document_filename: '${TEST_DOCUMENT_FILENAME}'
        }
      }
    },
    FileDirectionsQuestionnaire: {
      respondent2DQFileDirectionsQuestionnaire: {
        explainedToClient: ['CONFIRM'],
        oneMonthStayRequested: 'Yes',
        reactionProtocolCompliedWith: 'Yes'
      }
    },
    DisclosureOfElectronicDocuments: {
      respondent2DQDisclosureOfElectronicDocuments: {
        reachedAgreement: 'No',
        agreementLikely: 'Yes'
      }
    },
    DisclosureOfNonElectronicDocuments: {
      respondent2DQDisclosureOfNonElectronicDocuments: {
        directionsForDisclosureProposed: 'Yes',
        standardDirectionsRequired: 'Yes',
        bespokeDirections: 'directions'
      }
    },
    Experts: {
      respondent2DQExperts: {
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
      respondent2DQWitnesses: {
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
      respondent2DQLanguage: {
        evidence: 'WELSH',
        court: 'WELSH',
        documents: 'WELSH'
      }
    },
    Hearing: {
      respondent2DQHearing: {
        hearingLength: 'MORE_THAN_DAY',
        hearingLengthDays: '5',
        unavailableDatesRequired: 'Yes',
        unavailableDates: [
          element({
            date: date(10),
            who: 'James Foo'
          })
        ]
      }
    },
    DraftDirections: {
      respondent2DQDraftDirections: {
        document_url: '${TEST_DOCUMENT_URL}',
        document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
        document_filename: '${TEST_DOCUMENT_FILENAME}'
      }
    },
    RequestedCourt: {
      respondent2DQRequestedCourt: {
        responseCourtLocations: {
          list_items: [
            listElement(config.defendant2SelectedCourt)
          ],
          value:  listElement(config.defendant2SelectedCourt)
        },
        reasonForHearingAtSpecificCourt: 'No reasons',
        requestHearingAtSpecificCourt: 'Yes'
      }
    },
    HearingSupport: {},
    VulnerabilityQuestions: {
      respondent2DQVulnerabilityQuestions: {
        vulnerabilityAdjustmentsRequired: 'Yes',
        vulnerabilityAdjustments: 'Defendant 2 reasons'
      }
    },
    FurtherInformation: {
      respondent2DQFurtherInformation: {
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
      applicantSolicitor2ClaimStatementOfTruth: {}
    },
  },
  invalid: {
    ConfirmDetails: {
      futureDateOfBirth: {
        respondent2: {
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
        respondent2DQExperts: {
          details: [],
          expertRequired: 'Yes',
          expertReportsSent: 'NOT_OBTAINED',
          jointExpertSuitable: 'Yes'
        }
      }
    },
    Hearing: {
      past: {
        respondent2DQHearing: {
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
        respondent2DQHearing: {
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
