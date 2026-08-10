const {I} = inject();

module.exports = {

  fields: (partyChosenId) =>  ({
    partyChosen: {
      id: '#partyChosen',
      element: `#partyChosen_${partyChosenId}`
    }
  }),

  async selectParty(partyChosenId) {
    I.waitForElement(this.fields(partyChosenId).partyChosen.id);
    await I.runAccessibilityTest();
    await within(this.fields(partyChosenId).partyChosen.id, () => {
      I.click(this.fields(partyChosenId).partyChosen.element);
    });
    await I.clickContinue();
  }
};
