const {I} = inject();

module.exports = {

  fields: {
    respondentResponseIsSame: {
      id: '#respondentResponseIsSame_radio',
      options: {
        yes: '#respondentResponseIsSame_Yes',
        no: '#respondentResponseIsSame_No'
      }
    },
    respondentResponseIsSameForBothClaimants:{
      id: '#defendantSingleResponseToBothClaimants_radio',
      options: {
        yes: '#defendantSingleResponseToBothClaimants_Yes',
        no: '#defendantSingleResponseToBothClaimants_No'
      }
    }
  },

  async defendantsHaveSameResponse(defendantsHaveTheSameResponse = false) {
    I.waitForElement(this.fields.respondentResponseIsSame.id);
    await I.runAccessibilityTest();
    const options = this.fields.respondentResponseIsSame.options;
    await within(this.fields.respondentResponseIsSame.id, () => {
      I.click(defendantsHaveTheSameResponse ? options.yes : options.no);
    });
    await I.clickContinue();
  },

  async defendantsHaveSameResponseForBothClaimants(defendantsHaveTheSameResponse = false) {
    I.waitForElement(this.fields.respondentResponseIsSameForBothClaimants.id);
    await I.runAccessibilityTest();
    const options = this.fields.respondentResponseIsSameForBothClaimants.options;
    await within(this.fields.respondentResponseIsSameForBothClaimants.id, () => {
      I.click(defendantsHaveTheSameResponse ? options.yes : options.no);
    });
    await I.clickContinue();
  }
};
