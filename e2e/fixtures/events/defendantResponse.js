const { date, element, buildAddress, listElement} = require('../../api/dataHelper');
const config = require('../../config.js');
const uuid = require('uuid');
module.exports = {
  defendantResponse: (allocatedTrack = 'MULTI_CLAIM') => {
    return {
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
            partyID: `${uuid.v1()}`.substring(0, 16),
            partyTypeDisplayValue: 'Individual',
            flags: {
              partyName: 'Sir John Doe',
              roleOnCase: 'Defendant 1'
            }
          }
        },
        SingleResponse: {},
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
        ...(allocatedTrack === 'FAST_CLAIM' || allocatedTrack === 'MULTI_CLAIM'? {
          FixedRecoverableCosts: {
            respondent1DQFixedRecoverableCosts: {
              band: 'BAND_1',
              reasons: 'reasons',
              complexityBandingAgreed: 'Yes',
              isSubjectToFixedRecoverableCostRegime: 'Yes'
            }
          }
        } : {}),
        ...(allocatedTrack === 'FAST_CLAIM_NIHL' ? {
          FixedRecoverableCosts: {
            respondent1DQFixedRecoverableCosts: {
              band: 'BAND_2',
              reasons: 'reasons',
              complexityBandingAgreed: 'Yes',
              isSubjectToFixedRecoverableCostRegime: 'Yes'
            }
          }
        } : {}),
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' ? {
          FixedRecoverableCosts: {
            respondent1DQFixedRecoverableCostsIntermediate: {
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
        ...(allocatedTrack === 'INTERMEDIATE_CLAIM' || allocatedTrack === 'MULTI_CLAIM'? {
          DisclosureReport: {
            respondent1DQDisclosureReport: {
              disclosureFormFiledAndServed: 'Yes',
              disclosureProposalAgreed: 'No',
            }
          },
        }: {}),
        ...(allocatedTrack === 'SMALL_CLAIM' ? {
          DeterminationWithoutHearing:{
            deterWithoutHearingRespondent1: {
              deterWithoutHearingYesNo: 'No',
              deterWithoutHearingWhyNot: 'Incredibly valid reasons, Respondent 1'
            }
          },
        } : {}),
        Experts: {
          respondent1DQExperts: {
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
          respondent1DQWitnesses: {
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
          respondent1DQLanguage: {
            court: 'WELSH',
            documents: 'WELSH'
          }
        },
        Hearing: {
          respondent1DQHearing: {
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
          respondent1DQRequestedCourt: {
            responseCourtLocations: {
              list_items: [
                listElement(config.defendantSelectedCourt)
              ],
              value: listElement(config.defendantSelectedCourt)
            },
            reasonForHearingAtSpecificCourt: 'No reasons',
            requestHearingAtSpecificCourt: 'Yes'
          },
          respondent1DQRemoteHearing: {
            remoteHearingRequested: 'Yes',
            reasonForRemoteHearing: 'No reasons'
          },
        },
        HearingSupport: {
          respondent1DQHearingSupport: {
            supportRequirements: 'Yes',
            supportRequirementsAdditional: 'Additional support reasons'
          }
        },
        VulnerabilityQuestions: {
          respondent1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'Yes',
            vulnerabilityAdjustments: 'some reasons for vulnerability',
          }
        },
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
            respondent1DQHearing: {
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
            respondent1DQHearing: {
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
