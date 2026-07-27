const {I} = inject();

module.exports = {
  fields: {
    fullAdmitType: {
      id: '#defenceAdmitPartPaymentTimeRouteRequired',
      options: {
        immediately: 'Immediately',
        setDate: 'By a set date',
        repaymentPlan: 'repayment plan for my client'

      },
    },
  },

  async selectPaymentRoute(paymentType) {
     
    if (!this.fields.fullAdmitType.options.hasOwnProperty('paymentType')) {
      throw new Error(`Response type: ${paymentType} does not exist`);
    }
    I.waitForElement(this.fields.fullAdmitType.id);
    await I.runAccessibilityTest();
    await within(this.fields.fullAdmitType.id, () => {
    I.click(this.fields.fullAdmitType.options[paymentType]);
    });

    await I.clickContinue();
  }
};
