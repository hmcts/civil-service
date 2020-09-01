const { I } = inject();

module.exports = {

  fields: {
    claimantResponseDocument: {
      id: '#applicant1DefenceResponseDocument_file'
    }
  },

  async uploadResponseDocuments (file) {
    I.waitForElement(this.fields.claimantResponseDocument.id);
    await I.attachFile(this.fields.claimantResponseDocument.id, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  },
};

