const dataHelper = require('../api/dataHelper');
const config = require('../config');

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
        companyName: 'Test Inc',
        primaryAddress: {
          AddressLine1: `${config.address.buildingAndStreet.lineOne + ' - claimant'}`,
          AddressLine2: config.address.buildingAndStreet.lineTwo,
          AddressLine3: config.address.buildingAndStreet.lineThree,
          PostTown: config.address.town,
          County: config.address.county,
          Country: config.address.country,
          PostCode: config.address.postcode
        }
      }
    },
    applicant1LitigationFriend: {
      applicant1LitigationFriend: {
        required: 'Yes',
        fullName: 'Bob the litigant friend',
        hasSameAddressAsLitigant: 'No',
        primaryAddress: {
          AddressLine1: `${config.address.buildingAndStreet.lineOne + ' - litigant friend'}`,
          AddressLine2: config.address.buildingAndStreet.lineTwo,
          AddressLine3: config.address.buildingAndStreet.lineThree,
          PostTown: config.address.town,
          County: config.address.county,
          Country: config.address.country,
          PostCode: config.address.postcode
        }
      }
    },
    defendant: {
      respondent1: {
        type: 'ORGANISATION',
        organisationName: 'Test Defendant Org',
        primaryAddress: {
          AddressLine1: `${config.address.buildingAndStreet.lineOne + ' - defendant'}`,
          AddressLine2: config.address.buildingAndStreet.lineTwo,
          AddressLine3: config.address.buildingAndStreet.lineThree,
          PostTown: config.address.town,
          County: config.address.county,
          Country: config.address.country,
          PostCode: config.address.postcode
        }
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
