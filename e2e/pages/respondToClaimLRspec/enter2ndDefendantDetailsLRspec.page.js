const { I } = inject();

module.exports = {
  fields: {
    solicitor1Reference: {
      id: '#specAoSRespondent2HomeAddressRequired_radio',
      options: {
        yes: '#specAoSRespondent2HomeAddressRequired_Yes',
        no: '#specAoSRespondent2HomeAddressRequired_No'
      }
    },

  },

 async confirmDetails() {

    I.waitForElement(this.fields.solicitor1Reference.id);
    await I.runAccessibilityTest();
    const options = this.fields.solicitor1Reference.options;
    await within(this.fields.solicitor1Reference.id, async () => {
      await I.click(options.yes);
    });

    await I.clickContinue();
  }
};
