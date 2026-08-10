const { I } = inject();

module.exports = {

  async claimTimelineTemplate() {
    await I.see('timeline template');
    await I.runAccessibilityTest();
    await I.clickContinue();
  },
};

