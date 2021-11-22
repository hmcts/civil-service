const { I } = inject();

module.exports = {
  fields: {
    interest: {
      id: '#interestClaimOptions-SAME_RATE_INTEREST',
    },
  },

  async selectInterest() {
    I.waitForElement(this.fields.interest.id);
    await I.runAccessibilityTest();
    await I.click('Same rate for whole period of time');
    await I.clickContinue();
  },
};
