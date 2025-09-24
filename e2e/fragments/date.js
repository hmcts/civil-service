const {I} = inject();
const {dateNoWeekends} = require('../api/dataHelper');

module.exports = {

  fields: function (id) {
    return {
      day: `#${id}-day`,
      month: `#${id}-month`,
      year: `#${id}-year`,
    };
  },

  async enterDate(fieldId = '', plusDays = 28) {
    I.waitForElement(this.fields(fieldId).day);
    await I.runAccessibilityTest();
    const date = new Date();
    date.setDate(date.getDate() + plusDays);
    I.fillField(this.fields(fieldId).day, date.getDate());
    I.fillField(this.fields(fieldId).month, date.getMonth() + 1);
    I.fillField(this.fields(fieldId).year, date.getFullYear());
  },

  async enterDateNoWeekends(fieldId = '', plusDays = 28) {
    I.waitForElement(this.fields(fieldId).day);
    await I.runAccessibilityTest();
    const workingDayDateString = await dateNoWeekends(plusDays);
    let parts = workingDayDateString.split('-');
    let workingDayDate = new Date(parts[0], parts[1] - 1, parts[2]);
    I.fillField(this.fields(fieldId).day, workingDayDate.getDate());
    I.fillField(this.fields(fieldId).month, workingDayDate.getMonth() + 1);
    I.fillField(this.fields(fieldId).year, workingDayDate.getFullYear());
  }
};
