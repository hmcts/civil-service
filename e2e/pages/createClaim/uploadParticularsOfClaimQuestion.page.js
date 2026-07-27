const { I } = inject();

module.exports = {

  fields: {
    uploadQuestion: {
      id: '#uploadParticularsOfClaim',
      options: {
        yes: '#uploadParticularsOfClaim_Yes',
        no: '#uploadParticularsOfClaim_No'
      }
    },
  },

  async chooseYesUploadParticularsOfClaim() {
    I.waitForElement(this.fields.uploadQuestion.id);
    await I.runAccessibilityTest();
    await within(this.fields.uploadQuestion.id, () => {
      I.click(this.fields.uploadQuestion.options['yes']);
    });
    await I.clickContinue();
  },
};

