const {I} = inject();

module.exports = {

  fields: {
    proceed: {
      id: '#applicant1ProceedWithClaim',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    }
  },

  async proceedWithClaim() {
    I.waitForElement(this.fields.proceed.id);
    await I.runAccessibilityTest();
    await within(this.fields.proceed.id, () => {
      I.click(this.fields.proceed.options.yes);
    });
    await I.clickContinue();
  },

  async dropClaim() {
    I.waitForElement(this.fields.proceed.id);
    await I.runAccessibilityTest();
    await within(this.fields.proceed.id, () => {
      I.click(this.fields.proceed.options.no);
    });
    await I.clickContinue();
  }
};
