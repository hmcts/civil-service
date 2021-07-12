const { I } = inject();

module.exports = {

  fields: {
    uploadQuestion: {
      id: '#uploadParticularsOfClaim',
      options: {
        yes: 'Yes',
        no: 'No'
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

