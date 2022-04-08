const {I} = inject();

module.exports = {
  fields: {
    mediationType: {
      id: '#responseClaimMediationSpecRequired_radio',
      options: {
        yes: 'Yes',
        no: 'No',
      }
    }
  },

  async selectMediation(responseType) {

    I.waitForElement(this.fields.mediationType.id);
    await I.runAccessibilityTest();
    await within(this.fields.mediationType.id, () => {
    I.click(this.fields.mediationType.options[responseType]);
    });

    await I.clickContinue();
  }
};

