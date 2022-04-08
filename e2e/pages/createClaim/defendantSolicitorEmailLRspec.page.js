const { I } = inject();

module.exports = {

  fields: {

    emailAddress: '#respondentSolicitor1EmailAddress'
  },

  async enterSolicitorEmail() {
    I.waitForElement(this.fields.emailAddress);
    await I.runAccessibilityTest();
    I.fillField(this.fields.emailAddress, 'civilmoneyclaimsdemo@gmail.com');
    await I.clickContinue();
  },
};

