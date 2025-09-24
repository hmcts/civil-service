const {I} = inject();

module.exports = {
  fields: {
    enterBreathing_type: {
      id: '#enterBreathing_type',
      options: {
        standard: 'Standard Breathing Space',
        mentalHealth: 'Mental Health Crises Moratorium',
      }
    }
  },

  async selectBSType() {

    I.waitForElement(this.fields.enterBreathing_type.id);
    await I.runAccessibilityTest();
    await within(this.fields.enterBreathing_type.id, () => {
    I.click(this.fields.enterBreathing_type.options.standard);
    });

    await I.clickContinue();
  }
};

