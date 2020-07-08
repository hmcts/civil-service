const {I} = inject();

module.exports = {

  fields: {
    solicitorReference: '#solicitorReferences_solicitorReference',
    defendantReference: '#solicitorReferences_defendantReference',
  },

  async enterReferences() {
    I.fillField(this.fields.solicitorReference, 'Solicitor Reference');
    I.fillField(this.fields.defendantReference, 'Defendant Reference');
    await I.clickContinue();
  }
};

