const {I} = inject();

const CASE_HEADER = 'ccd-case-header > h1';
const CONFIRMATION_HEADER = 'ccd-markdown';

module.exports = {

  async submit(buttonText, expectedMessage) {
    I.waitForText(buttonText);
    await I.retryUntilExists(() => I.click(buttonText), CONFIRMATION_HEADER);
    I.see(expectedMessage);
  },

  returnToCaseDetails() {
    I.click('Close and Return to case details');
    I.waitForElement(CASE_HEADER);
  }
};
