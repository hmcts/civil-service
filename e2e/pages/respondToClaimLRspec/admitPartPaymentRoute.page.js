const {I} = inject();
const dataHelper = require('../../api/dataHelper');

module.exports = {
  fields: {
    partAdmitType: {
      id: '#defenceAdmitPartPaymentTimeRouteRequired',
      options: {
        immediately: 'Immediately',
        setDate: 'By a set date',
        repaymentPlan: 'repayment plan for my client'

      },
    },
    dayOfPayment: '#whenWillThisAmountBePaid-day',
    monthOfPayment: '#whenWillThisAmountBePaid-month',
    yearOfPayment: '#whenWillThisAmountBePaid-year',
  },

 async selectPaymentRoute(partAdmitType) {
   I.waitForElement(this.fields.partAdmitType.id);
   await I.runAccessibilityTest();
   await I.click(this.fields.partAdmitType.options[partAdmitType]);
   if ('setDate' == partAdmitType) {
      const date = dataHelper.incrementDate(new Date(), 0, 1, 0);
      await I.fillField(this.fields.dayOfPayment, `${date.getDate()}`);
      await I.fillField(this.fields.monthOfPayment, `${date.getMonth() + 1}`);
      await I.fillField(this.fields.yearOfPayment, `${date.getFullYear()}`);
    }

   await I.clickContinue();
  },
};
