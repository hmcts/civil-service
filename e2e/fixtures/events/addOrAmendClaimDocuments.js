const {document} = require('../../api/dataHelper');

module.exports = {
  valid: {
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: document('particularsOfClaim.pdf')
      }
    },
  },
  invalid: {
    Upload: {
      duplicateError: {
        servedDocumentFiles: {
          particularsOfClaimDocument: document('particularsOfClaim.pdf'),
          particularsOfClaimText: 'Some text'
        }
      },
      nullError: {
        servedDocumentFiles: {}
      }
    }
  }
};
