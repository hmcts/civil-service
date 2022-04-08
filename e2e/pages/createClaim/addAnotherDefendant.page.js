const {I} = inject();

module.exports = {

  fields: {
    addRespondent2: {
      id: '#addRespondent2',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterAddAnotherDefendant(addAnotherDefendant) {
    I.waitForElement(this.fields.addRespondent2.id);
    await I.runAccessibilityTest();
    await within(this.fields.addRespondent2.id, () => {
      const { yes, no } = this.fields.addRespondent2.options;
      I.click(addAnotherDefendant ? yes : no);
    });

    await I.clickContinue();
  }
};

