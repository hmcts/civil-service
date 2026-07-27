const date = require('../../../fragments/date');
const {I} = inject();

module.exports = {

  fields: {
    respondentDebtorOffer: {
      id: '#gaRespondentDebtorOffer_respondentDebtorOffer',
      options: {
        accept: 'I accept the debtor\'s offer',
        doNotAccept: 'I DO NOT accept the debtor\'s offer'
      }
    },
    paymentPlan: {
      id: '#gaRespondentDebtorOffer_paymentPlan',
      options: {
        monthlyPayment: 'I will accept the following instalments per month',
        fullPayment: 'I will accept payment in full by a set date'
      }
    },
    paymentDateId: 'paymentSetDate',
    monthlyInstalmentAmount: '#gaRespondentDebtorOffer_monthlyInstalment',
    debtorObjectionsTextArea: '#gaRespondentDebtorOffer_debtorObjections',
  },

  async selectDebtorOffer(type, paymentPlanType) {
    await I.waitInUrl('/RESPOND_TO_APPLICATIONGARespondentDebtorOffer');
    I.waitForElement(this.fields.respondentDebtorOffer.id);
    await within(this.fields.respondentDebtorOffer.id, () => {
      I.click(this.fields.respondentDebtorOffer.options[type]);
    });
    await I.see('Proposed payment plan');
    I.waitForElement(this.fields.paymentPlan.id);
    await within(this.fields.paymentPlan.id, () => {
      I.click(this.fields.paymentPlan.options[paymentPlanType]);
    });
    if ('fullPayment' === paymentPlanType) {
      await I.see('Proposed set date');
      await date.enterDate(this.fields.paymentDateId, +1);
    } else if ('monthlyPayment' === paymentPlanType) {
      await I.see('Proposed instalments per month');
      await I.fillField(this.fields.monthlyInstalmentAmount, '1000');
    }
    await I.fillField(this.fields.debtorObjectionsTextArea, 'Test Objections');
    await I.clickContinue();
  }
};

