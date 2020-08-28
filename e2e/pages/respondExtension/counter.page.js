const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    extensionCounter: {
      id: '#respondentSolicitor1claimResponseExtensionCounter',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    extensionCounterDate: {
      id: 'respondentSolicitor1claimResponseExtensionCounterDate',
    }
  },

  async enterCounterDate() {
    I.waitForElement(this.fields.extensionCounter.id);
    await I.see('Your current deadline is');
    await within(this.fields.extensionCounter.id, () => {
      I.click(this.fields.extensionCounter.options.yes);
    });

    await date.enterDate(this.fields.extensionCounterDate.id, 50);
    await I.clickContinue();
  }
};

