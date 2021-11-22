const { I } = inject();

module.exports = {
  fields: {
    interest: {
      id: '#interestClaimFrom-FROM_CLAIM_SUBMIT_DATE',
    },
  },

  async selectInterestDateStart() {
    I.waitForElement(this.fields.interest.id);
    await I.runAccessibilityTest();
    await I.click('The date you submit the claim. If you submit after 4pm it will be the next working day.');
    await I.clickContinue();
  },
};
