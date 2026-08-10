const {I} = inject();

module.exports = {
  fields: {
        dayOfBS: '#expectedEnd-day',
        monthOfBS: '#expectedEnd-month',
        yearOfBS: '#expectedEnd-year',
  },

  async liftBS() {

    await I.fillField(this.fields.dayOfBS, 1);
    await I.fillField(this.fields.monthOfBS, 5);
    await I.fillField(this.fields.yearOfBS, 2022);

    await I.clickContinue();
  }
};
