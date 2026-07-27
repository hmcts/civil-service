const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      cyaForm: '.check-your-answers',
    }
  },

  async verifyCOSCheckAnswerForm(claimantName, def1Name, def2Name, mpScenario) {
    I.waitInUrl('/submit');
    I.seeNumberOfVisibleElements(this.fields.checkAnswerForm.cyaForm, 1);
    I.see('Check your answers');
    I.see(claimantName);
    if (mpScenario === 'ONE_V_TWO_LIPS'){
      I.see(def1Name);
      I.see(def2Name);
    } else {
      I.see(def2Name);
    }
    I.seeNumberOfVisibleElements('.button', 2);
  },

  async verifyCOSSupportingEvidence() {
    I.seeNumberOfVisibleElements('.check-your-answers ccd-read-document-field button', 2);
    I.see('Supporting evidence');
  },
};


