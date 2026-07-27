const {I} = inject();

module.exports = {
  fields: {
    assistedOrder: {
      id: '#finalOrderSelection-ASSISTED_ORDER',
    },
    freeFormOrder: {
      id: '#finalOrderSelection-FREE_FORM_ORDER',
    }
  },

  async selectFreeFormOrder() {
    I.waitForElement(this.fields.freeFormOrder.id);
    await I.runAccessibilityTest();
    await I.click(this.fields.freeFormOrder.id);
    await I.clickContinue();
  },

  async selectAssistedOrder() {
    I.waitForElement(this.fields.assistedOrder.id);
    await I.runAccessibilityTest();
    await I.click(this.fields.assistedOrder.id);
    await I.clickContinue();
  },

};