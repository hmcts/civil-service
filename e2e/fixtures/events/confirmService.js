const {date, document, element} = require('../../api/dataHelper.js');

module.exports = {
  valid: {
    ServedDocuments: {
      servedDocuments: ['CLAIM_FORM', 'OTHER'],
      servedDocumentsOther: 'Test Document'
    },
    Upload: {
      servedDocumentFiles: {
        particularsOfClaim: [element(document('particularsOfClaim.pdf'))],
        other: [element(document('other.pdf'))]
      }
    },
    Method: {
      serviceMethodToRespondentSolicitor1: {
        type: 'POST'
      }
    },
    Location: {
      serviceLocationToRespondentSolicitor1: {
        location: 'RESIDENCE'
      }
    },
    Date: {
      serviceDateToRespondentSolicitor1: date()
    },
    StatementOfTruth: {
      applicant1ServiceStatementOfTruthToRespondentSolicitor1: {
        name: 'Foo Bar',
        role: 'Service Test Solicitor',
      }
    },
  },
  invalid: {
    ServedDocuments: {
      blankOtherDocuments: {
        servedDocuments: ['CLAIM_FORM', 'OTHER'],
        servedDocumentsOther: ' '
      }
    },
    Date: {
      yesterday: {
        serviceDateToRespondentSolicitor1: date(-1)
      },
      tomorrow: {
        serviceDateToRespondentSolicitor1: date(1)
      }
    }
  }
};
