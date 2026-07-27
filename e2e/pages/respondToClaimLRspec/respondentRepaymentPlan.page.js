const {I} = inject();
const dataHelper = require('../../api/dataHelper');

module.exports = {
  fields: {
    repaymentFrequency: {
      id: '#respondent1RepaymentPlan_repaymentFrequency',
      options: {
        week: 'Every week',
        twoWeek: 'Every 2 week',
        everyMonth: 'Every month'
      },
    },
      repaymentAmount: '#respondent1RepaymentPlan_paymentAmount',
      dayOfPayment: '#firstRepaymentDate-day',
      monthOfPayment: '#firstRepaymentDate-month',
      yearOfPayment: '#firstRepaymentDate-year',
  },

  async selectRepaymentPlan() {
    await I.fillField(this.fields.repaymentAmount,10);
    await within(this.fields.repaymentFrequency.id, () => {
    I.click(this.fields.repaymentFrequency.options.everyMonth);

    });
    const date = dataHelper.incrementDate(new Date(), 0, 2, 0);
    await I.fillField(this.fields.dayOfPayment, date.getDate());
    await I.fillField(this.fields.monthOfPayment, date.getMonth() + 1);
    await I.fillField(this.fields.yearOfPayment, date.getFullYear());


    await I.clickContinue();
  }
};
