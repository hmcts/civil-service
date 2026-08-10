const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      draftDirections: `#${party}DQDraftDirections`,
    };
  },

  async upload(party, file) {
    I.waitForElement(this.fields(party).draftDirections);
    await I.runAccessibilityTest();
    await I.attachFile(this.fields(party).draftDirections, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  }
};
