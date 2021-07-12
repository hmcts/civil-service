const {I} = inject();

module.exports = {

  fields: {
    detailsOfClaim: '#detailsOfClaim'
  },

  async enterDetailsOfClaim() {
    I.waitForElement(this.fields.detailsOfClaim);
    await I.runAccessibilityTest();
    I.fillField(this.fields.detailsOfClaim, 'Test details of claim');
    await I.clickContinue();
  }
};

