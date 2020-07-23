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
    const serviceDate = new Date();
    I.fillField(this.fields.serviceDate.day, serviceDate.getDate());
    I.fillField(this.fields.serviceDate.month, serviceDate.getMonth() +1);
    I.fillField(this.fields.serviceDate.year, serviceDate.getFullYear());

    await I.clickContinue();
  }
};

