const {date, buildAddress} = require('../../../api/dataHelper');
const uuid = require('uuid');

module.exports = {
  valid: {
    ConfirmNameAddress: {
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
    ResponseIntention: {
      respondent1ClaimResponseIntentionType: 'FULL_DEFENCE',
      respondent1ClaimResponseIntentionTypeApplicant2: 'FULL_DEFENCE'
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
