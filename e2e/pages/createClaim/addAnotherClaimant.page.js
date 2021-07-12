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

  async enterAddAnotherClaimant() {
    I.waitForElement(this.fields.addApplicant2.id);
    await I.runAccessibilityTest();
    await within(this.fields.addApplicant2.id, () => {
      I.click(this.fields.addApplicant2.options.no);
    });

    await I.clickContinue();
  }
};

