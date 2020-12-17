const {document, element, listElement} = require('../../api/dataHelper');
const address = require('../address');

const selectedPba = listElement('PBA0077597');

module.exports = {
  valid: {
    References: {
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant test reference',
        respondentSolicitor1Reference: 'Respondent test reference'
      }
    },
    Court: {
      courtLocation: {
        applicantPreferredCourt: 'Test Preferred Court'
      }
    },
    Claimant: {
      applicant1: {
        type: 'COMPANY',
        companyName: 'Test Inc',
        primaryAddress: {
          AddressLine1: `${address.buildingAndStreet.lineOne + ' - applicant'}`,
          AddressLine2: address.buildingAndStreet.lineTwo,
          AddressLine3: address.buildingAndStreet.lineThree,
          PostTown: address.town,
          County: address.county,
          Country: address.country,
          PostCode: address.postcode
        }
      }
    },
    ClaimantLitigationFriendRequired: {
      applicant1LitigationFriendRequired: 'Yes',
    },
    ClaimantLitigationFriend: {
      applicant1LitigationFriend: {
        fullName: 'Bob the litigant friend',
        hasSameAddressAsLitigant: 'No',
        primaryAddress: {
          AddressLine1: `${address.buildingAndStreet.lineOne + ' - litigant friend'}`,
          AddressLine2: address.buildingAndStreet.lineTwo,
          AddressLine3: address.buildingAndStreet.lineThree,
          PostTown: address.town,
          County: address.county,
          Country: address.country,
          PostCode: address.postcode
        }
      }
    },
    Defendant: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        individualDateOfBirth: null,
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
    },
    LegalRepresentation: {
      respondent1Represented: 'No'
    },
    ClaimType: {
      claimType: 'PERSONAL_INJURY'
    },
    PersonalInjuryType: {
      personalInjuryType: 'ROAD_ACCIDENT'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaim: [element(document('particularsOfClaim.pdf'))]
      }
    },
    ClaimValue: {
      claimValue: {
        statementOfValueInPennies: '3000000'
      }
    },
    PbaNumber: {
      applicantSolicitor1PbaAccounts: {
        list_items: [
          selectedPba,
          listElement('PBA0078094')
        ],
        value: selectedPba

      }
    },
    StatementOfTruth: {
      applicantSolicitor1ClaimStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      }
    }
  },
};
