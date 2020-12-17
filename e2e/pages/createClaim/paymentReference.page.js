const {I} = inject();

module.exports = {

  fields: {
    paymentReference: '#paymentReference',
  },

  async updatePaymentReference() {
    I.waitForElement(this.fields.paymentReference);
    I.clearField(this.fields.paymentReference);
    I.fillField(this.fields.paymentReference, 'abcdefg');
    await I.clickContinue();
  }
};

