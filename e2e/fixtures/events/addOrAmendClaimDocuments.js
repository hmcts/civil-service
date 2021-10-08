const {document, element} = require('../../api/dataHelper');

module.exports = {
  valid: {
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: [element(document('particularsOfClaim.pdf'))]
      }
    },
  },
  invalid: {
    Upload: {
      duplicateError: {
        servedDocumentFiles: {
          particularsOfClaimDocument: [element(document('particularsOfClaim.pdf'))],
          particularsOfClaimText: 'Some text'
        }
      },
    }
  }
};
