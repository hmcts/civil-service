const {I} = inject();

module.exports = {

  fields: {
    paymentReference: '#claimIssuedPaymentDetails_customerReference',
  },

  async updatePaymentReference() {
    I.waitForElement(this.fields.paymentReference);
    await I.runAccessibilityTest();
    I.clearField(this.fields.paymentReference);
    I.fillField(this.fields.paymentReference, 'abcdefg');
    await I.clickContinue();
  }
};

