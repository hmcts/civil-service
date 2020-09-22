const dataHelper = require('../api/dataHelper.js');

module.exports = {
  valid: {
    references: {
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant test reference',
        respondentSolicitor1Reference: 'Respondent test reference'
      }
    },
    court: {
      courtLocation: {
        applicantPreferredCourt: 'Test Preferred Court'
      }
    },
    claimant: {
      applicant1: {
        type: 'COMPANY',
        companyName: 'Test Inc'
      }
    },
    defendant: {
      respondent1: {
        type: 'ORGANISATION',
        organisationName: 'Test Defendant Org'
      }
    },
    claimType: {
      claimType: 'PERSONAL_INJURY'
    },
    personalInjuryType: {
      personalInjuryType: 'ROAD_ACCIDENT'
    },
    upload: {
      servedDocumentFiles: {
        particularsOfClaim: [dataHelper.document('testDocument.pdf')]
      }
    },
    claimValue: {
      claimValue: {
        lowerValue: '300',
        higherValue: '500'
      }
    },
    statementOfTruth: {
      applicantSolicitor1ClaimStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      }
    }
  },
  invalid: {
    claimValue: {
      claimValue: {
        lowerValue: '1000',
        higherValue: '500'
      },
    }
  }
};
