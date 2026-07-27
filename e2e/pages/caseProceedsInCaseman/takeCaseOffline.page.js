const {I} = inject();

module.exports = {

    async takeCaseOffline() {
      I.waitForText('Take offline');
      await I.runAccessibilityTest();
      I.click('Take offline');
    }

};
