const {document, element} = require('../../api/dataHelper');

module.exports = {
  valid: {
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: [
          {
            id: "65c881cd-cbd4-4dd8-8a7f-0419d390c83d",
            value: {
              document_url: "${TEST_DOCUMENT_URL}",
              document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
              document_filename: "${TEST_DOCUMENT_FILENAME}"
            }
          }
        ]
      }
    },
  },
  invalid: {
    Upload: {
      duplicateError: {
        servedDocumentFiles: {
          particularsOfClaimDocument: [
            {
              id: "65c881cd-cbd4-4dd8-8a7f-0419d390c83d",
              value: {
                document_url: "${TEST_DOCUMENT_URL}",
                document_binary_url: "${TEST_DOCUMENT_BINARY_URL}",
                document_filename: "${TEST_DOCUMENT_FILENAME}"
              }
            }
            ],
          particularsOfClaimText: 'Some text'
        }
      },
    }
  }
};
