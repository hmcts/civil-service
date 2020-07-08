const { I } = inject();

module.exports = {

  fields: {
    lowerValue: '#claimValue_lowerValue',
    higherValue: '#claimValue_higherValue',
  },

  async enterClaimValue() {
      I.fillField(this.fields.lowerValue, '1000');
      I.fillField(this.fields.higherValue, '10000');
      await I.clickContinue();
  }
};

