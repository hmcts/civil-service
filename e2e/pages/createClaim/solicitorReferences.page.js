const {I} = inject();

module.exports = {

  fields: {
    claimantReference: '#solicitorReferences_applicantSolicitor1Reference',
    defendantReference: '#solicitorReferences_respondentSolicitor1Reference',
  },

  async enterReferences() {
    I.waitForElement(this.fields.claimantReference);
    I.fillField(this.fields.claimantReference, 'Claimant Reference');
    I.fillField(this.fields.defendantReference, 'Defendant Reference');
    await I.clickContinue();
  }
};

