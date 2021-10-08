const {I} = inject();

module.exports = {

  fields: {
    solicitorReferences: {
      id: '#solicitorReferences_respondentSolicitor1Reference'
    }
  },

  async confirmReference() {
    I.waitForElement(this.fields.solicitorReferences.id);
    await I.runAccessibilityTest();
    I.fillField(this.fields.solicitorReferences.id, 'Defendant Reference ++');
    await I.clickContinue();
  }
};
