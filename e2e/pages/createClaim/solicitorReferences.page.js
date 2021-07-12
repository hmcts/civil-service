const {I} = inject();

module.exports = {

  fields: {
    applicantReference: '#solicitorReferences_applicantSolicitor1Reference',
    respondentReference: '#solicitorReferences_respondentSolicitor1Reference',
  },

  async enterReferences() {
    I.waitForElement(this.fields.applicantReference);
    await I.runAccessibilityTest();
    I.fillField(this.fields.applicantReference, 'Applicant Reference');
    I.fillField(this.fields.respondentReference, 'Respondent Reference');
    await I.clickContinue();
  }
};

