const {I} = inject();

module.exports = {

  fields: {
    respondentResponseIsSame: {
      id: '#respondentResponseIsSame',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async defendantsHaveSameResponse(defendantsHaveTheSameResponse = false) {
    I.waitForElement(this.fields.respondentResponseIsSame.id);
    await I.runAccessibilityTest();
    const options = this.fields.respondentResponseIsSame.options;
    await within(this.fields.respondentResponseIsSame.id, () => {
      I.click(defendantsHaveTheSameResponse ? options.yes : options.no);
    });
    await I.clickContinue();
  }
};
