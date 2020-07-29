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
    await within(this.fields.extensionCounter.id, () => {
      I.click(this.fields.extensionCounter.options.yes);
    });

    await date.enterDate(this.fields.extensionCounterDate.id);
    await I.clickContinue();
  }
};

