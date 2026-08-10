const {expect} = require('chai');
const date = require('../../../fragments/date');
const {I} = inject();

module.exports = {

  fields: {
    consentOrderForm: {
      id: '#approveConsentOrder_approveConsentOrder',
      heading: '#approveConsentOrder_approveConsentOrder h2',
      consentOrderDescription: '#approveConsentOrder_consentOrderDescription',
      dateID: 'consentOrderDateToEnd'
    },
  },

  async approveConsentOrder() {
    await I.waitInUrl('/APPROVE_CONSENT_ORDER/APPROVE_CONSENT_ORDERGAApproveConsentOrderScreen', 5);
    await I.waitForElement(this.fields.consentOrderForm.id);
    await I.seeTextEquals('Approve consent order', this.fields.consentOrderForm.heading);
    let orderDetails = await I.grabValueFrom(this.fields.consentOrderForm.consentOrderDescription);
    await expect(orderDetails).to.equals('Test Order details');
    await date.enterDate(this.fields.consentOrderForm.dateID, +2);
    await I.clickContinue();
  },
};

