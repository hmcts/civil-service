const address = require('../address');
const {date} = require('../../api/dataHelper');

module.exports = {
  valid: {
    ConfirmNameAddress: {},
    ConfirmDetails: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        individualDateOfBirth: date(-1),
        primaryAddress: {
          AddressLine1: `${address.buildingAndStreet.lineOne + ' - defendant'}`,
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
    ResponseIntention: {
      respondent1ClaimResponseIntentionType: 'FULL_DEFENCE'
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
            AddressLine1: `${address.buildingAndStreet.lineOne + ' - defendant'}`,
            AddressLine2: address.buildingAndStreet.lineTwo,
            AddressLine3: address.buildingAndStreet.lineThree,
            PostTown: address.town,
            County: address.county,
            Country: address.country,
            PostCode: address.postcode
          }
        }
      }
    }
  }
};
