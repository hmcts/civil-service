const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    extensionDate: {
      id: 'respondentSolicitor1AgreedDeadlineExtension',
    }
  },

  async enterExtensionDate() {
    await I.runAccessibilityTest();
    await date.enterDate(this.fields.extensionDate.id, 50);
    await I.clickContinue();
  }
};

