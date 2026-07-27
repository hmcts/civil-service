const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
    }
  },

  async verifyAppOrderCheckAnswerForm(caseNumber) {
    await I.waitInUrl('GENERATE_DIRECTIONS_ORDER/submit');
    await I.see('Check your answers');
    I.seeInCurrentUrl(caseNumber);
    I.seeNumberOfVisibleElements('.button', 2);
  },
};


