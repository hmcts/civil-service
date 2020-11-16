const {date, document, element} = require('../../api/dataHelper');
const address = require('../address');

module.exports = {
  valid: {
    RespondentResponseType: {
      respondent1ClaimResponseType: 'FULL_DEFENCE'
    },
    Upload: {
      respondent1ClaimResponseDocument: {
        file: document('claimResponse.pdf')
      }
    },
    ConfirmNameAddress: {},
    ConfirmDetails: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        individualDateOfBirth: date(-1),
        primaryAddress: {
          AddressLine1: `${address.buildingAndStreet.lineOne + ' - respondent'}`,
          AddressLine2: address.buildingAndStreet.lineTwo,
          AddressLine3: address.buildingAndStreet.lineThree,
          PostTown: address.town,
          County: address.county,
          Country: address.country,
          PostCode: address.postcode
        }
      },
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant test reference',
        respondentSolicitor1Reference: 'Respondent test reference'
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
      respondent1DQDisclosureOfNonElectronicDocuments: 'None'
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
    Hearing: {
      respondent1DQHearing: {
        hearingLength: 'MORE_THAN_DAY',
        hearingLengthDays: 5,
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
      respondent1DQDraftDirections: document('draftDirections.pdf')
    },
    RequestedCourt: {
      respondent1DQRequestedCourt: {
        name: 'Example court',
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
      respondent1DQStatementOfTruth: {
        name: 'John Doe',
        role: 'Tester'
      }
    }
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
          primaryAddress: {
            AddressLine1: `${address.buildingAndStreet.lineOne + ' - respondent'}`,
            AddressLine2: address.buildingAndStreet.lineTwo,
            AddressLine3: address.buildingAndStreet.lineThree,
            PostTown: address.town,
            County: address.county,
            Country: address.country,
            PostCode: address.postcode
          }
        }
      }
    },
    Experts: {
      emptyDetails: {
        respondent1DQExperts: {
          details: [],
          expertRequired: 'Yes',
          exportReportsSent: 'NOT_OBTAINED',
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
