const {I} = inject();

module.exports = {

  fields: {
    claimType: '#claimType',
  },

  async selectClaimType() {
    I.waitForElement(this.fields.claimType);
    I.selectOption(this.fields.claimType, 'Personal injury - road accident');
    await I.clickContinue();
  }
};

