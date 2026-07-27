const {I} = inject();

module.exports = {

  fields: {
    personalInjuryType: {
      id: '#personalInjuryType',
      options: {
        roadAccident: 'Road accident'
      }
    },
  },

  async selectPersonalInjuryType() {
    I.waitForElement(this.fields.personalInjuryType.id);
    await I.runAccessibilityTest();
    await within(this.fields.personalInjuryType.id, () => {
      I.click(this.fields.personalInjuryType.options.roadAccident);
    });
    await I.clickContinue();
  }
};

