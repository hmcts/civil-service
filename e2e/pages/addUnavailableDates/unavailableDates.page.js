const {I} = inject();
const date = require('../../fragments/date');

module.exports = {

  fields: function () {
    return {
      unavailableDates: {
          unavailableDateType: {
            options: {
              singleDateId: '#additionalUnavailableDates_0_unavailableDateType-SINGLE_DATE',
              dateRangeId: '#additionalUnavailableDates_1_unavailableDateType-DATE_RANGE'
            }
          },
          date: 'date',
          fromDate: 'fromDate',
          toDate: 'toDate'
      },
    };
  },

  async enterUnavailableDates() {
    await I.runAccessibilityTest();

    await I.addAnotherElementToCollection();
    await I.waitForElement(this.fields().unavailableDates.unavailableDateType.options.singleDateId);
    I.forceClick(this.fields().unavailableDates.unavailableDateType.options.singleDateId);
    await date.enterDate(this.fields().unavailableDates.date, 15);

    await I.addAnotherElementToCollection();
    await I.waitForElement(this.fields().unavailableDates.unavailableDateType.options.dateRangeId);
    I.forceClick(this.fields().unavailableDates.unavailableDateType.options.dateRangeId);
    await date.enterDate(this.fields().unavailableDates.fromDate, 30);
    await date.enterDate(this.fields().unavailableDates.toDate, 35);

    await I.clickContinue();
  },

  async confirmSubmission(url) {
    await I.amOnPage(url);
    await I.waitForText('Respondent 2 unavailable dates 3');
    await I.waitForText('Respondent 2 unavailable dates 4');
    await I.waitForText('Unavailability Dates Event');
  }
};

