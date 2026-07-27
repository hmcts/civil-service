const date = require('../../../fragments/date');
const {I} = inject();

module.exports = {

  fields: {
    applicationDetailDD: {
      id: '#gaHearingNoticeApplication_hearingNoticeApplicationDetail',
      options: {
        defendant: 'Defendant',
        claimant: 'Claimant',
        claimAndDef: 'Claimant and Defendant',
      }
    },
    typeOfApplication: '#gaHearingNoticeApplication_hearingNoticeApplicationType',
    applicationDate: 'hearingNoticeApplicationDate',
    errorMessage: '.error-message',
  },

  async verifyErrorMsg() {
    await I.waitInUrl('HEARING_SCHEDULED_GAHearingNoticeGADetail');
    I.waitForElement(this.fields.applicationDetailDD.id);
    await I.click('Continue');
    await I.seeNumberOfVisibleElements(this.fields.errorMessage, 3);
    I.see('Application details is required');
    I.see('Type of application is required');
    I.see('Date of application is required');
  },

  async fillApplicationDetails(party) {
    await I.waitInUrl('HEARING_SCHEDULED_GAHearingNoticeGADetail');
    I.waitForElement(this.fields.applicationDetailDD.id);
    I.selectOption(this.fields.applicationDetailDD.id, this.fields.applicationDetailDD.options[party]);
    await I.fillField(this.fields.typeOfApplication, 'Test App type');
    await date.enterDate(this.fields.applicationDate, +1);
    await I.clickContinue();
  },
};

