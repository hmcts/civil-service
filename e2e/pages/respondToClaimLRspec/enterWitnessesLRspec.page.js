const { I } = inject();

module.exports = {
fields: {
      id: '#responseClaimWitnesses',
},
  async howManyWitnesses() {
    await I.runAccessibilityTest();
    await I.fillField(this.fields.id,2);
    await I.clickContinue();
  },
};

