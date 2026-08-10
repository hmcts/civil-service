const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
    }
  },

  async respVerifyCheckAnswerForm(caseId) {
    I.seeInCurrentUrl('RESPOND_TO_APPLICATION/submit');
    I.see('Check your answers');
    I.seeInCurrentUrl(caseId);
    I.seeNumberOfVisibleElements('.button', 2);
    I.seeNumberOfVisibleElements('.case-field-change a', 2);
  },
  /* async clickOnChangeLink() {
     I.click({css: '.check-your-answers tr:nth-child(2) a'});
     I.seeInCurrentUrl('/RESPOND_TO_APPLICATION/RESPOND_TO_APPLICATIONGARespHearingScreen');
   },*/
};


