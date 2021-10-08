const { date, buildAddress } = require('../../api/dataHelper');

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
        primaryAddress: buildAddress('respondent')
      },
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
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
          primaryAddress: buildAddress('respondent')
        }
      }
    }
  }
};
