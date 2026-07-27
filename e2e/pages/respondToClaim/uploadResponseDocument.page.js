const { I } = inject();

module.exports = {

  fields: (party) => ({
    respondentResponseDocument: {
      id: `#${party}ClaimResponseDocument_file`
    }
  }),

  async uploadResponseDocuments (party, file) {
    I.waitForElement(this.fields(party).respondentResponseDocument.id);
    await I.runAccessibilityTest();
    await I.attachFile(this.fields(party).respondentResponseDocument.id, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  },
};

