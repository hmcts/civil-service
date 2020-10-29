const {I} = inject();

module.exports = {

  fields: {
    statementOfValue: '#claimValue_statementOfValueInPennies',
  },

  async enterClaimValue() {
    I.waitForElement(this.fields.statementOfValue);
    I.fillField(this.fields.statementOfValue, '30000');
    await I.clickContinue();
  }
};

