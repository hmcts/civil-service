const { I } = inject();

module.exports = {
  fields: {
    claimAmount: {
      id: '#claimAmountBreakup',
      details: '#claimAmountBreakup_0_claimReason',
      amount: '#claimAmountBreakup_0_claimAmount',
    },
  },

  async addClaimItem(claimAmount) {
    I.waitForElement(this.fields.claimAmount.id);
    await I.runAccessibilityTest();
    I.click('Add new');
    I.fillField(this.fields.claimAmount.details, 'Test claim item details');
    I.fillField(this.fields.claimAmount.amount, claimAmount);
    await I.clickContinue();
  },
};
