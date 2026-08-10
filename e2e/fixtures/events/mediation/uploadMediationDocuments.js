const {listElementWithCode, date} = require('../../../api/dataHelper');
const uuid = require('uuid');

module.exports = {
  uploadMediationDocuments: (user, sameDefendantSolicitor = false) => {
    let partyChosen;
    if (user === 'claimant') {
      partyChosen = {
        uploadMediationDocumentsPartyChosen: {
          list_items: [
            listElementWithCode('CLAIMANT_1', 'Claimant 1: Test Inc')
          ],
          value: listElementWithCode('CLAIMANT_1', 'Claimant 1: Test Inc')
        },
      };
    } else {
      if (sameDefendantSolicitor) {
        partyChosen = {
          uploadMediationDocumentsPartyChosen: {
            list_items: [
              listElementWithCode('DEFENDANT_1', 'Defendant 1: Mr John Doe'),
              listElementWithCode('DEFENDANT_2', 'Second Defendant'),
              listElementWithCode('DEFENDANTS', 'Defendants 1 and 2')
            ],
            value: listElementWithCode('DEFENDANTS', 'Defendants 1 and 2')
          },
        };
      } else {
        if (user === 'defendant') {
          partyChosen = {
            uploadMediationDocumentsPartyChosen: {
              list_items: [
                listElementWithCode('DEFENDANT_1', 'Defendant 1: Mr John Doe')
              ],
              value: listElementWithCode('DEFENDANT_1', 'Defendant 1: Mr John Doe')
            },
          };
        }
        if (user === 'defendantTwo') {
          partyChosen = {
            uploadMediationDocumentsPartyChosen: {
              list_items: [
                listElementWithCode('DEFENDANT_2', 'Defendant 2: Dr Foo Bar')
              ],
              value: listElementWithCode('DEFENDANT_2', 'Defendant 2: Dr Foo Bar')
            },
          };
        }
      }
    }
    return {
      userInput: {
        WhoIsDocumentFor: {
          ...partyChosen,
        },
        DocumentType: {
          mediationDocumentsType: ['NON_ATTENDANCE_STATEMENT', 'REFERRED_DOCUMENTS'],
        },
        DocumentUpload: {
          nonAttendanceStatementForm: [
            {
              id: uuid.v1(),
              value: {
                document: {
                  document_url: '${TEST_DOCUMENT_URL}',
                  document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                  document_filename: '${TEST_DOCUMENT_FILENAME}'
                },
                documentDate: date(-1),
                yourName: 'name'
              },
            }
          ],
          documentsReferredForm: [
            {
              id: uuid.v1(),
              value: {
                document: {
                  document_url: '${TEST_DOCUMENT_URL}',
                  document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                  document_filename: '${TEST_DOCUMENT_FILENAME}'
                },
                documentDate: date(-1),
                documentType: 'type'
              },
            },
          ],
        },
      },
    };
  }
};
