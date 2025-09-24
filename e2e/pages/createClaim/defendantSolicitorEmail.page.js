const {I} = inject();

module.exports = {

  fields: respondentSolicitorNumber => {
    return {
      emailAddress: `#respondentSolicitor${respondentSolicitorNumber}EmailAddress`
    };
  },

  async enterSolicitorEmail(respondentSolicitorNumber) {
    I.waitForElement(this.fields(respondentSolicitorNumber).emailAddress);
    await I.runAccessibilityTest();
    I.fillField(this.fields(respondentSolicitorNumber).emailAddress, 'civilunspecified@gmail.com');
    await I.clickContinue();
  },
};

