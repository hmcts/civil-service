const uuid = require('uuid');
const docUuid = uuid.v1();

module.exports = {
  valid: {
    Upload: {
      servedDocumentFiles: {
        particularsOfClaimDocument: [
          {
            id: docUuid,
            value: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
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
              id: docUuid,
              value: {
                document_url: '${TEST_DOCUMENT_URL}',
                document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                document_filename: '${TEST_DOCUMENT_FILENAME}'
              }
            }
            ],
          particularsOfClaimText: 'Some text'
        }
      },
    }
  }
};
