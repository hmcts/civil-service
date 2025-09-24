const { date, buildAddress } = require('../../../api/dataHelper');
const uuid = require('uuid');

module.exports = {
  midEventData:{
    SolicitorReferences:{
      solicitorReferences:{
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference'
      },
    }
  },
  valid: {
    ConfirmNameAddress: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        primaryAddress: buildAddress('respondent'),
        individualDateOfBirth: date(-1),
        partyName: 'Sir John Doe',
        partyID: `${uuid.v1()}`.substring(0, 16),
        partyTypeDisplayValue: 'Individual',flags: {
          partyName: 'Sir John Doe',
          roleOnCase: 'Defendant 1'
        }
      },
      respondent2: {
        type: 'INDIVIDUAL',
        individualFirstName: 'Foo',
        individualLastName: 'Bar',
        individualTitle: 'Dr',
        primaryAddress: buildAddress('second respondent'),
        individualDateOfBirth: date(-1),
        partyName: 'Dr Foo Bar',
        partyTypeDisplayValue: 'Individual',
        flags: {
          partyName: 'Dr Foo Bar',
          roleOnCase: 'Defendant 2'
        }
      }
    },
    ResponseIntention: {
      respondent2ClaimResponseIntentionType: 'FULL_DEFENCE'
    },
    SolicitorReferences: {
      respondentSolicitor2Reference: 'sol2reference',
      solicitorReferences: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference',
      },
      solicitorReferencesCopy: {
        applicantSolicitor1Reference: 'Applicant reference',
        respondentSolicitor1Reference: 'Respondent reference',
      }
    },
  }
};
