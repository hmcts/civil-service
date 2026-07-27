const uuid = require('uuid');
const docUuid = uuid.v1();

module.exports = {
  upload: (typeOfNote) => {
    const caseNoteData = {
      CaseNoteSelection: {
        caseNoteType: typeOfNote
      }
    };

    if (typeOfNote === 'NOTE_ONLY') {
      caseNoteData.CaseNoteSelectionNote = {
        caseNoteTA: 'string'
      };
    } else if (typeOfNote === 'DOCUMENT_ONLY') {
      caseNoteData.UploadDocument = {
        documentAndNameToAdd: [{
          id: docUuid,
          value: {
            documentName: 'string',
            document: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            }
          }
        }]
      };
    } else {
      caseNoteData.UploadDocumentAndNote = {
        documentAndNoteToAdd: [{
          id: docUuid,
          value: {
            documentName: 'string',
            document: {
              document_url: '${TEST_DOCUMENT_URL}',
              document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
              document_filename: '${TEST_DOCUMENT_FILENAME}'
            },
            documentNote: 'string'
          }
        }]
      };
    }

    return { valid: caseNoteData };
  }
};
