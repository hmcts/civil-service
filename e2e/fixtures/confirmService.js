const dataHelper = require('../api/dataHelper.js');

module.exports = {
  valid: {
    servedDocuments: {
      servedDocuments: ['CLAIM_FORM']
    },
    upload: {
      servedDocumentFiles: {
        particularsOfClaim: [dataHelper.document('testDocument.pdf')]
      }
    },
    method: {
      serviceMethodToRespondentSolicitor1: {
        type: 'POST'
      }
    },
    location: {
      serviceLocationToRespondentSolicitor1: {
        location: 'RESIDENCE'
      }
    },
    date: {
      serviceDateToRespondentSolicitor1: dataHelper.date()
    },
    statementOfTruth: {
      applicant1ServiceStatementOfTruthToRespondentSolicitor1: {
        name: 'Foo Bar',
        role: 'Service Test Solicitor',
      }
    },
  },
  invalid: {
    date: {
      yesterday: {
        serviceDateToRespondentSolicitor1: dataHelper.date(-1)
      },
      tomorrow: {
        serviceDateToRespondentSolicitor1: dataHelper.date(1)
      }
    }
  }
};
