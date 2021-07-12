const {I} = inject();

module.exports = {

  fields: {
    statementOfValue: '#claimValue_statementOfValueInPennies',
  },

  async enterClaimValue() {
    I.waitForElement(this.fields.statementOfValue);
    await I.runAccessibilityTest();
    I.fillField(this.fields.statementOfValue, '30000');
    await I.clickContinue();
  }
};

