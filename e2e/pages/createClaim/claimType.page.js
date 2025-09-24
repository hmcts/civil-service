const {I} = inject();

module.exports = {

  fields: {
    claimType: {
      id: '#claimType',
      options: {
        personalInjury: 'Personal injury'
      }
    },
  },

  async selectClaimType() {
    I.waitForText(this.fields.claimType.options.personalInjury, 60);
    await I.runAccessibilityTest();
    I.click(this.fields.claimType.options.personalInjury);
    await I.clickContinue();
  }
};

