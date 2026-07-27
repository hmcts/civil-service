const {date, element, buildAddress, listElement} = require('../../../api/dataHelper');
const uuid = require('uuid');

module.exports = {
  defendantResponse: (allocatedTrack = 'MULTI_CLAIM') => {
    return {
      valid: {
        ConfirmDetails: {
          respondent2: {
            type: 'INDIVIDUAL',
            partyID: `${uuid.v1()}`.substring(0, 16),
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
        FileDirectionsQuestionnaire: {
          respondent2DQFileDirectionsQuestionnaire: {
            explainedToClient: ['CONFIRM'],
            oneMonthStayRequested: 'Yes',
            reactionProtocolCompliedWith: 'Yes'
          }
        },
        ...(allocatedTrack === 'FAST_CLAIM' || allocatedTrack === 'MULTI_CLAIM'? {
          FixedRecoverableCosts: {
            respondent2DQFixedRecoverableCosts: {
              band: 'BAND_3',
              reasons: 'reasons',
              complexityBandingAgreed: 'Yes',
              isSubjectToFixedRecoverableCostRegime: 'Yes'
            }
          }
        } : {}),
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' ? {
          FixedRecoverableCosts: {
            respondent2DQFixedRecoverableCostsIntermediate: {
              band: 'BAND_2',
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
        ...(allocatedTrack != 'SMALL_CLAIM' ? {
          DisclosureReport: {
            respondent2DQDisclosureReport: {
              disclosureFormFiledAndServed: 'Yes',
              disclosureProposalAgreed: 'No',
            }
          }
        } : {}),
        ...(allocatedTrack === 'SMALL_CLAIM' ? {
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent2: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, Respondent 2'
            }
          },
        } : {}),
        Experts: {
          respondent2DQExperts: {
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
          respondent2DQWitnesses: {
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
          respondent2DQLanguage: {
            court: 'WELSH',
            documents: 'WELSH'
          }
        },
        Hearing: {
          respondent2DQHearing: {
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
        RequestedCourt: {
          respondent2DQRequestedCourt: {
            responseCourtLocations: {
              list_items: [
                listElement('High Wycombe Law Courts - THE LAW COURTS, EASTON STREET - HP11 1LR')
              ],
              value: listElement('High Wycombe Law Courts - THE LAW COURTS, EASTON STREET - HP11 1LR')
            },
            reasonForHearingAtSpecificCourt: 'No reasons',
            requestHearingAtSpecificCourt: 'Yes'
          }
        },
        HearingSupport: {
          respondent2DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Witness John Smith: Requires support worker'
          }
        },
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
            respondent2DQHearing: {
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
            respondent2DQHearing: {
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
        },
      }
    };
  }
};
