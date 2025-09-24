const { I } = inject();

module.exports = {

  fields: {
    reference: '#respondentSolicitor2Reference'
  },

  async enterReference() {
    I.waitForElement(this.fields.reference);
    await I.runAccessibilityTest();
    I.fillField(this.fields.reference, 'some reference');
    await I.clickContinue();
  },
};

