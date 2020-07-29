const {I} = inject();

const statementOfTruth = require('../../fragments/statementOfTruth');

module.exports = {

  fields: {
    lowerValue: '#claimValue_lowerValue',
    higherValue: '#claimValue_higherValue',
  },

  async enterClaimValue() {
    I.waitForElement(this.fields.lowerValue);
    I.fillField(this.fields.lowerValue, '1000');
    I.fillField(this.fields.higherValue, '10000');
    await I.retryUntilExists(() => I.clickContinue(), statementOfTruth.fields('claim').name);
  }
};

