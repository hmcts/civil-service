const { I } = inject();

module.exports = {
  fields: {
    claimTimelineOption: {
      id: '#specClaimTimeline',
    },
  },

  async addTimeline() {
    I.waitForElement(this.fields.claimTimelineOption.id);
    await I.runAccessibilityTest();
    I.click('Add new');
    I.waitForElement('#timelineDate-day');
    I.fillField('#timelineDate-day', 1);
    I.fillField('#timelineDate-month', 1);
    I.fillField('#timelineDate-year', 2021);
    I.fillField('#timelineOfEvents_0_timelineDescription', 'Test details of claim');
    await I.clickContinue();
  },
};
