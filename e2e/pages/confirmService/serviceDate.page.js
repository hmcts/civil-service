const {I} = inject();

module.exports = {

  fields: {
    serviceDate: {
      day: '#serviceDate-day',
      month: '#serviceDate-month',
      year: '#serviceDate-year',
    }
  },

  async enterServiceDate() {
    I.fillField(this.fields.serviceDate.day, '1');
    I.fillField(this.fields.serviceDate.month, '1');
    I.fillField(this.fields.serviceDate.year, '2099');

    await I.clickContinue();
  }
};

