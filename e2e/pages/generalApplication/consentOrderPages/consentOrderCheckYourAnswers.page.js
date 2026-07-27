const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
    }
  },

  async verifyConsentOrderCheckAnswerForm(caseNumber, num) {
    await I.waitInUrl('APPROVE_CONSENT_ORDER/submit');
    await I.see('Check your answers');
    await I.see('Test Order details');
    I.seeInCurrentUrl(caseNumber);
    I.seeNumberOfVisibleElements('.button', 2);
    I.seeNumberOfVisibleElements('.case-field-change a', num);
  },
};


