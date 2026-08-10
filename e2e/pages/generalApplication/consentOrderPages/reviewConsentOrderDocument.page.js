const {expect} = require('chai');
const {docFullDate} = require('../../generalAppCommons');
const {I} = inject();

module.exports = {

  fields: {
    previewDocFields: {
      caseFieldLabel: '.case-field .case-field__label',
      documentLink: '.case-field__value button'
    }
  },

  async reviewOrderDocument() {
    await I.waitInUrl('/APPROVE_CONSENT_ORDERGAConsentOrderDocPreviewScreen');
    I.seeNumberOfVisibleElements('.button', 2);
    let docURL = await I.grabTextFrom(locate(this.fields.previewDocFields.documentLink));
    expect(docURL).to.contains(`Consent_order_for_application_${docFullDate}`);
    await I.seeTextEquals('Draft Order', this.fields.previewDocFields.caseFieldLabel);
    await I.clickContinue();
  }
};


