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
    I.waitForElement(this.fields.claimType.id);
    await I.runAccessibilityTest();
    await within(this.fields.claimType.id, () => {
      I.click(this.fields.claimType.options.personalInjury);
    });
    await I.clickContinue();
  }
};

