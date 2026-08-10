const {I} = inject();

module.exports = {

  fields: {
    viewDetails: 'button[id^="link-view-details"]',
    cancelEle: 'button[id^="link-cancel"]',
    cancelOption: '#listerr',
    cancellationRequestedText: 'Cancellation requested',
  },

  async clickCancelHearing() {
    await I.waitForElement(this.fields.cancelEle);
    const urlBefore = await I.grabCurrentUrl();
    let firstAttempt = true;
    await I.retryUntilUrlChanges(async () => {
      if(!firstAttempt)
        await I.refreshPage();
      await I.waitForText('Current and upcoming');
      await I.forceClick(locate(this.fields.cancelEle).first());
      firstAttempt = false;
    }, urlBefore);
    await I.waitForText('Are you sure you want to cancel this hearing?');
    await I.runAccessibilityTest();
    await I.forceClick(this.fields.cancelOption);
    await I.clickContinue();
  },

  async verifyHearingCancellation() {
    await I.waitForText('Current and upcoming');
    await I.runAccessibilityTest();
    await I.see(this.fields.cancellationRequestedText.toUpperCase());
    await I.click(locate(this.fields.viewDetails).first());
    await I.waitForText(this.fields.cancellationRequestedText);
  }
};
