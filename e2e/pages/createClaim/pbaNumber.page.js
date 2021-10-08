const {I} = inject();

module.exports = {

  fields: {
    pbaNumber: {
      id: '#applicantSolicitor1PbaAccounts',
      options: {
        activeAccount: 'PBA0088192'
      }
    }
  },

  async selectPbaNumber() {
    I.waitForElement(this.fields.pbaNumber.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.pbaNumber.id, this.fields.pbaNumber.options.activeAccount);
    await I.clickContinue();
  }
};

