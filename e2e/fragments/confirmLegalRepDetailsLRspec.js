const { I } = inject();

module.exports = {
  fields: {
    id: '#specAoSRespondentCorrespondenceAddressRequired_Yes',
  },

  async confirmDetails() {
    I.waitForElement(this.fields.id);
    await I.runAccessibilityTest();
    await I.click('Yes');
    await I.clickContinue();
  },
};
