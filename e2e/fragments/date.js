const {I} = inject();

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
  }
};
