const {I} = inject();

module.exports = {

  fields: {

    nonAttendanceStatementForm: {
      addNewButton: '#nonAttendanceStatementForm button',
      name: '#nonAttendanceStatementForm_0_yourName',
      day: '#documentDate-day',
      month: '#documentDate-month',
      year: '#documentDate-year',
      document: '#nonAttendanceStatementForm_0_document'
    },

    documentsReferredForm: {
      addNewButton: '#documentsReferredForm button',
      docType: '#documentsReferredForm_0_documentType',
      day: '#documentsReferredForm #documentDate-day',
      month: '#documentsReferredForm #documentDate-month',
      year: '#documentsReferredForm #documentDate-year',
      document: '#documentsReferredForm_0_document'
    },
  },

  async fillNonAttendanceStatement(file) {
    await I.waitForText('You cannot withdraw a document once you have uploaded it', 10);
    await I.see('Mediation non-attendance');
    I.click(this.fields.nonAttendanceStatementForm.addNewButton);
    I.fillField(this.fields.nonAttendanceStatementForm.name, 'test name');
    I.fillField(this.fields.nonAttendanceStatementForm.day, '1');
    I.fillField(this.fields.nonAttendanceStatementForm.month, '1');
    I.fillField(this.fields.nonAttendanceStatementForm.year, '2022');
    I.wait(5); //rate limiting on doc uplods - EXUI-1194
    await I.attachFile(this.fields.nonAttendanceStatementForm.document, file);
  },

  async fillDocumentsReferredForm(file) {
    await I.see('Documents referred to in statement');
    I.click(this.fields.documentsReferredForm.addNewButton);
    await I.see('Document type');
    I.fillField(this.fields.documentsReferredForm.docType, 'test document');
    I.fillField(this.fields.documentsReferredForm.day, '1');
    I.fillField(this.fields.documentsReferredForm.month, '10');
    I.fillField(this.fields.documentsReferredForm.year, '2022');
    I.wait(5); //rate limiting on doc uplods - EXUI-1194
    await I.attachFile(this.fields.documentsReferredForm.document, file);
  },
};
