const {I} = inject();

module.exports = {

  fields: {
    solicitorReferences: {
      id: '#solicitorReferences_defendantReference'
    }
  },

  async confirmReference() {
    I.waitForElement(this.fields.solicitorReferences.id);
    I.fillField(this.fields.solicitorReferences.id, 'Updated Defendant Reference');

    await I.clickContinue();
  }
};
