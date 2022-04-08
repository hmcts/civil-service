const {I} = inject();

module.exports = {

  fields: {
    addApplicant2: {
      id: '#addApplicant2',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterAddAnotherClaimant(addAnotherClaimant) {
    I.waitForElement(this.fields.addApplicant2.id);
    await I.runAccessibilityTest();
    await within(this.fields.addApplicant2.id, () => {
      const { yes, no } = this.fields.addApplicant2.options;
      I.click(addAnotherClaimant ? yes : no);
    });

    await I.clickContinue();
  }
};

