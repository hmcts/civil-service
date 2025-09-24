const {I} = inject();

module.exports = {
  fields: {
    fullAdmitType: {
      id: '#specDefenceAdmittedRequired_radio',
      claimOwingAmount: '#respondToAdmittedClaimOwingAmount',
      options: {
        yes: '#specDefenceAdmittedRequired_Yes',
        no: '#specDefenceAdmittedRequired_No',
      }
    }
  },

  async selectFullAdmitType(fullAdmitType) {

    I.waitForElement(this.fields.fullAdmitType.id);
    await I.runAccessibilityTest();
    await within(this.fields.fullAdmitType.id, () => {
    I.click(this.fields.fullAdmitType.options[fullAdmitType]);
    });
    await I.fillField(this.fields.fullAdmitType.claimOwingAmount,100);
    await I.clickContinue();
  }
};


