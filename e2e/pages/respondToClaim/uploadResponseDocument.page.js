const { I } = inject();

module.exports = {

  fields: {
    respondentResponseDocument: {
      id: '#respondent1ClaimResponseDocument_file'
    }
  },

  async uploadResponseDocuments (file) {
    I.waitForElement(this.fields.respondentResponseDocument.id);
    await I.runAccessibilityTest();
    await I.attachFile(this.fields.respondentResponseDocument.id, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  },
};

