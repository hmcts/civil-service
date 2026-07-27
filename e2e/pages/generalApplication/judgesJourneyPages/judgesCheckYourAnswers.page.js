const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
    }
  },

  async verifyJudgesCheckAnswerForm(caseNumber) {
    I.seeInCurrentUrl('MAKE_DECISION/submit');
    I.see('Check your answers');
    I.seeInCurrentUrl(caseNumber);
    I.seeNumberOfVisibleElements('.button', 2);
  },
};


