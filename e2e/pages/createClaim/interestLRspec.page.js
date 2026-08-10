const { I } = inject();

module.exports = {
  fields: {
    interest: {
      id: '#claimInterest_Yes',
    },
  },

  async addInterest() {
    I.waitForElement(this.fields.interest.id);
    await I.runAccessibilityTest();
    await I.click('Yes');
    await I.clickContinue();
  },
};
