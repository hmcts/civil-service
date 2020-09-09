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
    await within(this.fields.claimType.id, () => {
      I.click(this.fields.claimType.options.personalInjury);
    });
    await I.clickContinue();
  }
};

