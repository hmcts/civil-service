const {I} = inject();

module.exports = {
  fields: {
    fullAdmitType: {
      id: '#specDefenceFullAdmittedRequired_radio',
      options: {
        yes: '#specDefenceFullAdmittedRequired_Yes',
        no: '#specDefenceFullAdmittedRequired_No',
      }
    }
  },

  async selectFullAdmitType(fullAdmitType) {

    I.waitForElement(this.fields.fullAdmitType.id);
    await I.runAccessibilityTest();
    await within(this.fields.fullAdmitType.id, () => {
    I.click(this.fields.fullAdmitType.options[fullAdmitType]);
    });

    await I.clickContinue();
  }
};


