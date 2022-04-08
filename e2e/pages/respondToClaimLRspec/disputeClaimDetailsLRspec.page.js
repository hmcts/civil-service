const {I} = inject();

module.exports = {

fields: {
      id: '#detailsOfWhyDoesYouDisputeTheClaim',
},
  async enterReasons() {
    await I.runAccessibilityTest();
    await I.fillField(this.fields.id,'defendant wants to  disputes the claim');
    await I.clickContinue();
  },
};

