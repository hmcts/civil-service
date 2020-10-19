const {I} = inject();

const statementOfTruth = require('../../fragments/statementOfTruth');

module.exports = {

  fields: {
    statementOfValue: '#claimValue_statementOfValueInPennies',
  },

  async enterClaimValue() {
    I.waitForElement(this.fields.statementOfValue);
    I.fillField(this.fields.statementOfValue, '30000');
    await I.retryUntilExists(() => I.clickContinue(), statementOfTruth.fields.claim.name);
  }
};

