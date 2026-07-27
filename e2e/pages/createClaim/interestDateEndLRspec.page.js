const { I } = inject();

module.exports = {
  fields: {
    interest: {
      id: '#interestClaimUntil-UNTIL_CLAIM_SUBMIT_DATE',
    },
  },

  async selectInterestDateEnd() {
    I.waitForElement(this.fields.interest.id);
    await I.runAccessibilityTest();
    await I.click('Until the claim is settled or judgment made.');
    await I.clickContinue();
  },
};
