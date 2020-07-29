const {I} = inject();

module.exports = {

  fields: {
    extensionResponse: '#respondentSolicitor1claimResponseExtensionRejectionReason'
  },

  async enterResponse() {
    I.waitForElement(this.fields.extensionResponse);
    I.fillField(this.fields.extensionResponse, 'Response to extension');
    await I.clickContinue();
  }
};

