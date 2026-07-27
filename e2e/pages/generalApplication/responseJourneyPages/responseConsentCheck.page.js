const {I} = inject();

module.exports = {

  fields: {
    respondentAgreementHasAgreed: {
      id: '#generalAppRespondent1Representative_hasAgreed',
      options: {
        yes: '#generalAppRespondent1Representative_hasAgreed_Yes',
        no: '#generalAppRespondent1Representative_hasAgreed_No'
      }
    },
  },

  async selectConsentCheck(consentCheck) {
    I.waitForElement(this.fields.respondentAgreementHasAgreed.id);
    I.seeInCurrentUrl('RESPOND_TO_APPLICATION/RESPOND_TO_APPLICATIONGARespondent1RespScreen');
    await within(this.fields.respondentAgreementHasAgreed.id, () => {
      I.click(this.fields.respondentAgreementHasAgreed.options[consentCheck]);
    });
    await I.clickContinue();
  }
};

