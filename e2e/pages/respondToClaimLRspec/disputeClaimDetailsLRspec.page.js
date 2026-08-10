const {I} = inject();

module.exports = {

fields: {
      id: '#detailsOfWhyDoesYouDisputeTheClaim',
      id2: '#detailsOfWhyDoesYouDisputeTheClaim2',
},
  async enterReasons(twoDefendants) {
    await I.runAccessibilityTest();
    if(twoDefendants){
        await I.fillField(this.fields.id2,'2nd defendant wants to  disputes the claim');
    }else{
        await I.fillField(this.fields.id,'defendant wants to  disputes the claim');
    }
    await I.clickContinue();
  },
};

