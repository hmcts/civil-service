const {I} = inject();

module.exports = {

  fields: {
    checkAnswerForm: {
      classname: '.check-your-answers',
      changeLink: '.case-field-change a',
      uploadEvidenceLink: '.collection-field-table button'
    }
  },

  async verifyCheckAnswerForm(caseId, consentCheck) {
    I.seeInCurrentUrl('/INITIATE_GENERAL_APPLICATION/submit');
    await I.see('Check your answers');
    I.seeInCurrentUrl(caseId);
    I.seeNumberOfVisibleElements('.button', 2);
    if ('yes' === consentCheck) {
      I.seeNumberOfVisibleElements(this.fields.checkAnswerForm.changeLink, 9);
    } else if ('no' === consentCheck) {
      I.seeNumberOfVisibleElements(this.fields.checkAnswerForm.changeLink, 10);
    } else {
      I.seeNumberOfVisibleElements(this.fields.checkAnswerForm.changeLink, 11);
    }
    I.seeTextEquals('examplePDF.pdf', this.fields.checkAnswerForm.uploadEvidenceLink);
  },

  async clickOnChangeLink(consentCheck) {
    if ('yes' === consentCheck) {
      I.click({css: '.check-your-answers tr:nth-child(8) a'});
    } else {
      I.click({css: '.check-your-answers tr:nth-child(9) a'});
    }
    I.seeInCurrentUrl('/INITIATE_GENERAL_APPLICATIONHearingDetails');
  },
};


