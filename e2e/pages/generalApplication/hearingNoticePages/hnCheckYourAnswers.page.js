const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
    }
  },

  async verifyNoticeCheckAnswerForm(caseNumber) {
    await I.waitInUrl('HEARING_SCHEDULED_GA/submit');
    await I.see('Check your answers');
    I.seeInCurrentUrl(caseNumber);
    I.seeNumberOfVisibleElements('.button', 2);
    I.seeNumberOfVisibleElements('.case-field-change a', 3);
  },
};


