const events = require('../../fixtures/ga-events/ga-ccd/events');
const {I} = inject();

module.exports = {

  fields: {
    summaryTab: 'div.mat-tab-label-content',
    nextStep: '#next-step option',
    spinner: 'div.spinner-container',
    n245FormLink: '[id$="generalAppN245FormUpload"] button'
  },

  async verifySummaryPage() {
    await I.waitForInvisible(locate(this.fields.spinner).withText('Loading'), 20);
    I.waitInUrl('#Application');
    I.see('Application');
    I.see('Parent Case ID');
    I.see('Hearing details');
    I.see('Preferred location');
    I.dontSee(events.RESPOND_TO_APPLICATION.name);
  },

  async verifyN245FormElements() {
    await I.waitForInvisible(locate(this.fields.spinner).withText('Loading'), 20);
    await I.see('N245 Form');
    await I.seeNumberOfVisibleElements(this.fields.n245FormLink, 1);
  },

  async verifyNoServiceReqElements() {
    await I.seeNumberOfVisibleElements(this.fields.summaryTab, 2);
    await I.dontSee('Service Request', this.fields.summaryTab);
  }
};

