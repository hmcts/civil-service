const {I} = inject();

module.exports = {

  fields: {
    idamEmailIsCorrect: {
      id: '#applicantSolicitor1CheckEmail_correct',
      options: {
        yes: 'Yes',
        no: 'No'
      },
      newEmail: '#applicantSolicitor1UserDetails_email',
    }
  },

  async enterUserEmail() {
    I.waitForElement(this.fields.idamEmailIsCorrect.id);
    await I.runAccessibilityTest();
    I.click(this.fields.idamEmailIsCorrect.options.yes);
    await I.clickContinue();
  }
};

