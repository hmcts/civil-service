const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: respondentSolicitorNumber => {
    return {
      extensionDate: {
        id: `respondentSolicitor${respondentSolicitorNumber}AgreedDeadlineExtension`,
      }
    };
  },

  async enterExtensionDate(respondentSolicitorNumber) {
    await I.runAccessibilityTest();
    await date.enterDateNoWeekends(this.fields(respondentSolicitorNumber).extensionDate.id, 40);
  }
};

