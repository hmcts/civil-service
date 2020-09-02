const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    serviceDate: {
      id: 'serviceDateToRespondentSolicitor1',
    }
  },

  async enterServiceDate() {
    await date.enterDate(this.fields.serviceDate.id, 0);
    await I.clickContinue();
  }
};

