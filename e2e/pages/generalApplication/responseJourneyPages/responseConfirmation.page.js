const {I} = inject();
const expect = require('chai').expect;

module.exports = {

  fields: {
    confirmation: {
      id: '#confirmation-body'
    },
    applicationList: '#confirmation-body li',
    errorMessage: 'ul.error-summary-list li',
    goButton: 'Go',
  },

  async verifyRespConfirmationPage() {
    I.seeInCurrentUrl('RESPOND_TO_APPLICATION/confirm');
    I.seeTextEquals('You have provided the requested information', '#confirmation-header h1');
  },

  async verifyRespApplicationType(appTypes) {
    I.waitForElement(this.fields.confirmation.id);
    appTypes.forEach(type => {
      return I.see(type);
    });
  },

  async closeAndReturnToCaseDetails() {
    await I.click('Close and Return to case details');
    await I.see('Respond to application');
  },

  async verifyAlreadyRespondedErrorMessage() {
    await I.retryUntilExists(async () => {
      await I.click(this.fields.goButton);
    }, this.fields.errorMessage);
    let actualErrorMsg = await I.grabTextFrom(this.fields.errorMessage);
    expect(actualErrorMsg).to.equals('The application has already been responded to.');
  }
};

