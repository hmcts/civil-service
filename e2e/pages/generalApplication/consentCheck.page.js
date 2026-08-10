const {I} = inject();

module.exports = {

  fields: {
    respondentAgreementHasAgreed: {
      id: '#generalAppRespondentAgreement_hasAgreed',
      options: {
        yes: '#generalAppRespondentAgreement_hasAgreed_Yes',
        no: '#generalAppRespondentAgreement_hasAgreed_No'
      }
    },
  },

  async selectConsentCheck(consentCheck) {
    await I.waitForElement(this.fields.respondentAgreementHasAgreed.id);
    I.seeInCurrentUrl('INITIATE_GENERAL_APPLICATIONGARespondentAgreementPage');
    await within(this.fields.respondentAgreementHasAgreed.id, () => {
      I.click(this.fields.respondentAgreementHasAgreed.options[consentCheck]);
    });
    await I.clickContinue();
  },

  async notInN245FormPage() {
    await I.seeInCurrentUrl('/INITIATE_GENERAL_APPLICATIONGARespondentAgreementPage');
    await I.see('A judge will still need to approve this order', '#generalAppRespondentAgreement_hasAgreed span');
    await I.dontSeeInCurrentUrl('/INITIATE_GENERAL_APPLICATIONGAUploadN245Form');
    await I.dontSee('Upload your completed N245 form');
  }
};

