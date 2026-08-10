const date = require('../../fragments/date');
const {I} = inject();

module.exports = {

  fields: {
    generalAppHearingSchedule: {
      id: '#generalAppHearingDate_hearingScheduledPreferenceYesNo',
      options: {
        yes: '#generalAppHearingDate_hearingScheduledPreferenceYesNo-Yes',
        no: '#generalAppHearingDate_hearingScheduledPreferenceYesNo-No'
      }
    },
    hearingScheduledDate: 'hearingScheduledDate',
  },

  async selectHearingScheduled(hearingScheduled) {
    await I.waitForElement(this.fields.generalAppHearingSchedule.id);
    await I.waitInUrl('INITIATE_GENERAL_APPLICATIONGAHearingDate');
    await I.see('Hearing scheduled related to this application');
    await within(this.fields.generalAppHearingSchedule.id, () => {
      I.click(this.fields.generalAppHearingSchedule.options[hearingScheduled]);
    });

    if ('yes' === hearingScheduled) {
      await I.see('Please provide the hearing dates.');
      await date.enterDate(this.fields.hearingScheduledDate, +1);
    }

    await I.clickContinue();
  }
};

