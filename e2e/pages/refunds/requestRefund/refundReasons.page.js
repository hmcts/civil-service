const {I} = inject();

module.exports = {
  fields: {
    refundreasons: {
      selectoptions: '//select[@id=\'sort\']'
    }
  },

  async chooseRefundsReason(reasonId) {
    I.waitForText('System/technical error');
    I.wait(2);
    I.see('Why are you making this refund?','h1');
    I.see('System/technical error');
    I.checkOption(`//input[@id='${reasonId}']`);
    I.click('Continue');
  }
};
