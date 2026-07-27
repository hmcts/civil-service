const {I} = inject();

module.exports = {

  fields: {
    statementOfValue: '#claimValue_statementOfValueInPennies',
  },

  async enterClaimValue(claimValue = 30000) {
    I.waitForElement(this.fields.statementOfValue);
    await I.runAccessibilityTest();
    I.fillField(this.fields.statementOfValue, claimValue);
    await I.clickContinue();
  }
};

