const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    extensionProposedDeadline: {
      id: 'respondentSolicitor1claimResponseExtensionProposedDeadline',
    }
  },

  async enterExtensionProposedDeadline() {
    await date.enterDate(this.fields.extensionProposedDeadline.id, 50);
    await I.clickContinue();
  }
};

