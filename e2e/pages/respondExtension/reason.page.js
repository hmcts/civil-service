const {I} = inject();

module.exports = {

  fields: {
    extensionResponse: '#respondentSolicitor1claimResponseExtensionRejectionReason'
  },

  async enterResponse() {
    I.fillField(this.fields.extensionResponse, 'Response to extension');
    await I.clickContinue();
  }
};

