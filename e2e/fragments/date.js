const {I} = inject();
const {expect} = require('chai');
const {dateNoWeekends} = require('../api/dataHelper');

const getDate = days => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date;
};

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
  },

  dateNoWeekendsBankHolidayNextDay: async function dateNoWeekendsBankHolidayNextDay(days = 0) {
    const date = getDate(days);
    let date_String = date.toISOString().slice(0, 10);
    let isDateABankHoliday = false;
    if (date.getDay() !== 6 && date.getDay() !== 0) {
      try {
        const rawBankHolidays = await fetch('https://www.gov.uk/bank-holidays.json');
        const ukbankholidays = await rawBankHolidays.json();
        isDateABankHoliday = JSON.stringify(ukbankholidays['england-and-wales'].events).includes(date_String);
        if (!isDateABankHoliday) {
          return date_String;
        } else {
          return await dateNoWeekendsBankHolidayNextDay(days + 1);
        }
      } catch (err) {
        console.warn('Error while fetching UK Bank Holidays...', err);
      }
    } else {
      return await dateNoWeekendsBankHolidayNextDay(days + 1);
    }
  },

  async verifyPrePopulatedDate(fieldId, orderType, workingDay) {
    I.waitForElement(this.fields(fieldId).day);
    let date = new Date();

    if (orderType !== 'assistedOrder') {
      date = new Date(workingDay);
    }

    let docMonth = ((date.getMonth() + 1) >= 10) ? (date.getMonth() + 1) : '0' + (date.getMonth() + 1);
    let twoDigitDate = ((date.getDate()) >= 10) ? (date.getDate()) : '0' + (date.getDate());

    let expectedDay = await I.grabValueFrom(this.fields(fieldId).day);
    await expect(expectedDay).to.equals(twoDigitDate.toString());

    let expectedMonth = await I.grabValueFrom(this.fields(fieldId).month);
    await expect(expectedMonth).to.equals(docMonth.toString());

    let expectedYear = await I.grabValueFrom(this.fields(fieldId).year);
    await expect(expectedYear).to.equals(date.getFullYear().toString());
  }
};
