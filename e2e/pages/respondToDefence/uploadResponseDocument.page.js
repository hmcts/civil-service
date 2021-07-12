const { I } = inject();

module.exports = {

  fields: {
    applicantResponseDocument: {
      id: '#applicant1DefenceResponseDocument_file'
    }
  },

  async uploadResponseDocuments (file) {
    I.waitForElement(this.fields.applicantResponseDocument.id);
    await I.runAccessibilityTest();
    await I.attachFile(this.fields.applicantResponseDocument.id, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  },
};

