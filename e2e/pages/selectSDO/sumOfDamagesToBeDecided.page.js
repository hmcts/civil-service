const {I} = inject();

module.exports = {
  fields: {
    drawDirectionsOrderRequired: {
      id: '#drawDirectionsOrderRequired_radio',
      options: {
        yes: '#drawDirectionsOrderRequired_Yes',
        no: '#drawDirectionsOrderRequired_No'
      }
    },
    sdoJudgementSum: '#drawDirectionsOrder_judgementSum'
  },

  async damagesToBeDecided(damages) {
    await I.runAccessibilityTest();
    await within(this.fields.drawDirectionsOrderRequired.id, () => {
      const { yes, no } = this.fields.drawDirectionsOrderRequired.options;
      I.click(damages ? yes : no);
    });
    if(damages){
      I.waitForElement(this.fields.sdoJudgementSum);
      I.fillField(this.fields.sdoJudgementSum, '75');
    }
    await I.clickContinue();
  }
};
