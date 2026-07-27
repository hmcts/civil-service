const {I} = inject();

module.exports = {
  fields: {
    reasonForRequest: {
      id: '#reasonForReconsiderationApplicant_reasonForReconsiderationTxt',
    },
    reasonForRequestReconsideration: '#reasonForReconsiderationApplicant_reasonForReconsiderationTxt',
  },

  async reasonForReconsideration() {
    I.waitForElement(this.fields.reasonForRequest.id);
    await I.runAccessibilityTest();
    I.fillField(this.fields.reasonForRequestReconsideration, 'Testing Request for Reconsideration');
    await I.clickContinue();
  }
};
