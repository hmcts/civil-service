const {I} = inject();

module.exports = {

  fields: {
    extensionAlreadyAgreed: {
      id: '#respondentSolicitor1claimResponseExtensionAlreadyAgreed',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    extensionReason: '#respondentSolicitor1claimResponseExtensionReason'
  },

  async selectAlreadyAgreed() {
    I.waitForElement(this.fields.extensionAlreadyAgreed.id);
    await within(this.fields.extensionAlreadyAgreed.id, () => {
      I.click(this.fields.extensionAlreadyAgreed.options.no);
    });
    I.fillField(this.fields.extensionReason, 'I need more time');

    await I.clickContinue();
  }
};

