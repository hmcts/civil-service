const {I} = inject();
const config = require('./../../config');

module.exports = {
  fields: {
    courtLocation: {
      id: '#hearingLocation',
      options: {
        preferredCourt: config.claimantSelectedCourt
      }
    },
   selectChannel: {
          id: '#channel',
          options: {
               person: '#channel-IN_PERSON',
               video: '#channel-VIDEO',
               telephone: '#channel-TELEPHONE',
          }
   },
   dayOfHearing: '#hearingDate-day',
   monthOfHearing: '#hearingDate-month',
   yearOfHearing: '#hearingDate-year',
   hearingTimeHourMinute: '#hearingTimeHourMinute',
   hearingDuration: '#hearingDuration',

  },

  async selectCourt() {
      I.waitForElement(this.fields.courtLocation.id);
      await I.runAccessibilityTest();
      I.selectOption(this.fields.courtLocation.id, this.fields.courtLocation.options.preferredCourt);
      I.click(this.fields.selectChannel.options.person);
      I.selectOption(this.fields.hearingTimeHourMinute, '08:00');
      I.selectOption(this.fields.hearingDuration, '30 minutes');
      I.fillField(this.fields.dayOfHearing, 1);
      I.fillField(this.fields.monthOfHearing, 12);
      I.fillField(this.fields.yearOfHearing, 2025);
      await I.clickContinue();
  }

};
