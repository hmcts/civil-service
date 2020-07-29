const { I } = inject();

module.exports = {

  fields: {
    extensionProposedDeadline: {
      day: '#respondentSolicitor1claimResponseExtensionProposedDeadline-day',
      month: '#respondentSolicitor1claimResponseExtensionProposedDeadline-month',
      year: '#respondentSolicitor1claimResponseExtensionProposedDeadline-year'
    }
  },

  async enterExtensionProposedDeadline () {
    I.waitForElement(this.fields.extensionProposedDeadline.day);
    const proposedDeadline = new Date();
    proposedDeadline.setDate(proposedDeadline.getDate() + 28);
    I.fillField(this.fields.extensionProposedDeadline.day, proposedDeadline.getDate());
    I.fillField(this.fields.extensionProposedDeadline.month, proposedDeadline.getMonth() + 1);
    I.fillField(this.fields.extensionProposedDeadline.year, proposedDeadline.getFullYear());

    await I.clickContinue();
  }
};

