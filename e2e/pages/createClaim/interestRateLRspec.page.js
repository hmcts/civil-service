const { I } = inject();

module.exports = {
  fields: {
    interest: {
      id: '#sameRateInterestSelection_sameRateInterestType-SAME_RATE_INTEREST_8_PC',
    },
  },

  async selectInterestRate() {
    I.waitForElement(this.fields.interest.id);
    await I.runAccessibilityTest();
    await I.click('8%');
    await I.clickContinue();
  },
};
