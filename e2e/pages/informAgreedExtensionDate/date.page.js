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
    await date.enterDate(this.fields(respondentSolicitorNumber).extensionDate.id, 50);
    await I.clickContinue();
  }
};

