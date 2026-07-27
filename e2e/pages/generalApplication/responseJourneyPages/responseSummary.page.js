// const events = require('../../../fixtures/ga-events/ga-ccd/events');
const {I} = inject();

module.exports = {

  fields: {
    summaryTab: 'div.mat-tab-label-content',
    nextStep: '#next-step option',
  },

  async verifySummaryPageAfterResponding() {
    I.seeInCurrentUrl('cases/case-details/');
    I.wait(1);
    I.see('Application');
    I.see('Parent Case ID');
    I.see('Hearing details');
    I.see('Preferred location');
    // I.dontSee(events.RESPOND_TO_APPLICATION.name);
    I.seeTextEquals('examplePDF.pdf', '.Application ccd-read-document-field > button');
    I.seeNumberOfVisibleElements('.Application a', 2);
    I.see('Respondent hearing details');
    I.see('Vulnerability questions');
  }
};

