const { I } = inject();
const config = require('../../config.js');

module.exports = {
  fields: {
    fixedCosts: {
      claimFixedCosts: '#fixedCosts_claimFixedCosts_Yes',
      fixedCostAmount: '#fixedCosts_fixedCostAmount'
    },
  },

  async addFixedCosts() {
    await I.waitForElement(this.fields.fixedCosts.claimFixedCosts);
    await I.runAccessibilityTest();
    await I.click('Yes');
    await I.fillField(this.fields.fixedCosts.fixedCostAmount, '100');
    await I.clickContinue();
  },
};
