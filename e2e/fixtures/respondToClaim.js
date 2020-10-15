module.exports = {
  valid: {
    experts: {
      disclosureReport: {
        respondent1DQDisclosureReport: {
          disclosureProposalAgreed: 'No',
          disclosureFormFiledAndServed: 'Yes'
        },
      },
      disclosureOfElectronicDocuments: {
        respondent1DQDisclosureOfElectronicDocuments: {
          reachedAgreement: 'Yes'
        },
      },
      respondent1DQExperts: {
        details: [{
          id: '555fed98-6ba0-48d2-bedc-4046a68fc2f4',
          value: {
            name: 'John Smith',
            whyRequired: 'Reason why required',
            estimatedCost: '10000',
            fieldOfExpertise: 'Science'
          }
        }],
        'expertRequired': 'Yes',
        'exportReportsSent': 'Yes',
        'jointExpertSuitable': 'Yes'
      },
    },
    hearing: {
      respondent1DQHearing: {
        hearingLength: 'LESS_THAN_DAY',
        unavailableDates: [{
          id: '53c0b9f3-261a-42cb-bf2c-9e63ebf224fd',
          value: {
            who: 'John Smith',
            date: '2020-11-04'
          }
        }],
        hearingLengthHours: '5',
        unavailableDatesRequired: 'Yes'
      },
    },
    witnesses: {
      respondent1DQWitnesses: {
        details: [{
          id: 'c49e24ed-cea7-48af-8bb0-9ee834f6d621',
          value: {
            name: 'John Smith',
            reasonForWitness: 'Reason for witness'
          }
        }],
        witnessesToAppear: 'Yes'
      },
    },
    hearingSupport: {
      respondent1DQHearingSupport: {
        otherSupport: 'Some support',
        requirements: ['SIGN_INTERPRETER', 'LANGUAGE_INTERPRETER', 'OTHER_SUPPORT'],
        signLanguageRequired: 'A language',
        languageToBeInterpreted: 'A language'
      },
    },
    requestedCourt: {
      respondent1DQRequestedCourt: {
        name: 'A court name',
        requestHearingAtSpecificCourt: 'Yes',
        reasonForHearingAtSpecificCourt: 'A reason for the court'
      },
    },
    statementOfTruth: {
      respondent1DQStatementOfTruth: {
        name: 'John Smith',
        role: 'Solicitor'
      },
    },
    furtherInformation: {
      respondent1DQFurtherInformation: {
        futureApplications: 'Yes',
        otherInformationForJudge: 'Other information for judge',
        reasonForFutureApplications: 'Reason for future applications'
      },
    },
    fileDirectionQuestionnaire: {
      respondent1DQFileDirectionsQuestionnaire: {
        explainedToClient: ['CONFIRM'],
        oneMonthStayRequested: 'No',
        reactionProtocolCompliedWith: 'No',
        reactionProtocolNotCompliedWithReason: 'Reason for not complying'
      },
    },
    disclosureOfNonElectronicDocuments: {
      respondent1DQDisclosureOfNonElectronicDocuments: 'Reason for no agreement',
    },
  },
  invalid: {}
};
