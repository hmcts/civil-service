const {I} = inject();

module.exports = {

  fields: {
    courtLocation: '#courtLocation_applicantPreferredCourt'
  },

  async enterCourt() {
    I.waitForElement(this.fields.courtLocation);
    await I.runAccessibilityTest();
    I.fillField(this.fields.courtLocation, '344');
    await I.clickContinue();
  }
};

