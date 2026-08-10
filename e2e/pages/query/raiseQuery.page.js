const {I} = inject();

module.exports = {
  fields: {
    query: {
      id: '[id="Raise a new query"]',
    }
  },

  async selectQuery() {
    I.waitForElement(this.fields.query.id);
    await I.runAccessibilityTest();
    I.click(this.fields.query.id);
    await I.clickContinue();
  }
};
