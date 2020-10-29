const {I} = inject();

module.exports = {

  fields: {
    pbaNumber: {
      id: '#pbaNumber',
      options: {
        activeAccount: 'PBA0077597'
      }
    }
  },

  async selectPbaNumber() {
    I.waitForElement(this.fields.pbaNumber.id);
    I.selectOption(this.fields.pbaNumber.id, this.fields.pbaNumber.options.activeAccount);
    await I.clickContinue();
  }
};

