const {I} = inject();

module.exports = {
  fields: {
    sdoOrderType: {
      id: '#orderType',
      options: {
        disposal: '#orderType-DISPOSAL',
        decideDamages: '#orderType-DECIDE_DAMAGES'
      }
    }
  },

  async decideOrderType(orderType) {
    await I.runAccessibilityTest();
    if(orderType === 'disposal'){
      I.click(this.fields.sdoOrderType.options.disposal);
    }
    else{
      I.click(this.fields.sdoOrderType.options.decideDamages);
    }
    await I.clickContinue();
  }
};
