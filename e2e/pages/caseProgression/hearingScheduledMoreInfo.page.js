const {I} = inject();

module.exports = {

fields: {
      id: '#information',
},
  async enterMoreInfo() {
    await I.runAccessibilityTest();
    await I.fillField(this.fields.id,'more info on hearing scheduled');
    await I.clickContinue();
  },
};