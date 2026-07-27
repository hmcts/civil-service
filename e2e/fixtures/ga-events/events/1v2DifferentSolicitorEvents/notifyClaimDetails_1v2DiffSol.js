const uuid = require('uuid');
const {listElement} = require('../../../../api/dataHelper');
const docUuid = uuid.v1();
const selectedNotifyOption = listElement('Both');

module.exports = {
  valid: {
    validateNotificationOption:{
      defendantSolicitorNotifyClaimDetailsOptions: {
        list_items: [
          selectedNotifyOption,
          listElement('Defendant One: Sir John Doe'),
          listElement('Defendant Two: Dr Foo Bar')
        ],
        value: selectedNotifyOption
      }
    },
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
