const {I} = inject();

module.exports = {
  fields: {
    decisionOnRequest: {
      id: '#decisionOnRequestReconsiderationOptions-CREATE_SDO',
    },
  },

  async selectCreateNewSDO() {
    I.waitForElement(this.fields.decisionOnRequest.id);
    await I.runAccessibilityTest();
    I.click('#decisionOnRequestReconsiderationOptions-CREATE_SDO');
    await I.clickContinue();
  },

  async selectYesOptionToUpholdThePreviousOrderMade() {
    I.waitForElement(this.fields.decisionOnRequest.id);
    await I.runAccessibilityTest();
    I.click('#decisionOnRequestReconsiderationOptions-YES');
    await I.clickContinue();
  },

  async selectNoOptionForPreviousOrderNeedsAmending() {
    I.waitForElement(this.fields.decisionOnRequest.id);
    await I.runAccessibilityTest();
    I.click('#decisionOnRequestReconsiderationOptions-CREATE_GENERAL_ORDER');
    await I.clickContinue();
  }
};
