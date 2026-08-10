const {I} = inject();

module.exports = {
  async continue() {
    await I.runAccessibilityTest();
    await I.clickContinue();
  },
};
