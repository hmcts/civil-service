const {I} = inject();

module.exports = {

  fields: {
    pbaNumber: {
      id: '#applicantSolicitor1PbaAccounts',
      options: {
        activeAccount: 'PBA0088192'
      }
    },
    claimFeeAmountToPay:
      '//h2[normalize-space()="Claim fee"]' +
      '/following::dt[normalize-space()="Amount to pay"][1]' +
      '/following-sibling::dd//span',

    otherRemedyAmountToPay:
      '//h2[normalize-space()="Other Remedy fee"]' +
      '/following::dt[normalize-space()="Amount to pay"][1]' +
      '/following-sibling::dd//span'
  },

  async selectPbaNumber() {
    I.waitForElement(this.fields.pbaNumber.id);
    I.selectOption(
      this.fields.pbaNumber.id,
      this.fields.pbaNumber.options.activeAccount
    );
    await I.clickContinue();
  },

  async clickContinue(claimType) {
    if (claimType === 'Personal injury') {
      await I.clickContinue();
    } else if (claimType === 'Housing disrepair') {
      await this.verifyClaimAmountToPay('£1,250.00');
      await this.verifyOtherRemedyAmountToPay('£387.00');
      await I.clickContinue();
    } else if(claimType === 'Damages and other remedy'){
      await this.verifyClaimAmountToPay('£1,250.00');
      await I.dontSeeElement(this.fields.otherRemedyAmountToPay);
      await I.clickContinue();
    } else{
      await I.clickContinue();
    }
  },

  async verifyClaimAmountToPay(expectedAmount) {
    I.waitForElement(this.fields.claimFeeAmountToPay);
    I.see(expectedAmount, this.fields.claimFeeAmountToPay);
  },

  async verifyOtherRemedyAmountToPay(expectedAmount) {
    I.waitForElement(this.fields.otherRemedyAmountToPay);
    I.see(expectedAmount, this.fields.otherRemedyAmountToPay);
  }
};

