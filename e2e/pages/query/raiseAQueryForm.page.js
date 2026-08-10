const date = require('../../fragments/date');
const { I } = inject();

module.exports = {
  fields: {
    query: {
      id: '#subject-hint',
      subject: '#subject',
      detail: 'textarea#body',
      nonHearingRelated: '#isHearingRelated-no',
      hearingRelated: '#isHearingRelated-yes',
      cya:'.govuk-summary-list',
      hearingDate: {
        id: 'hearingDate'
      },
    },
  },

  async enterQueryDetails() {
    await I.runAccessibilityTest();
    I.waitForElement(this.fields.query.id);
    I.fillField(this.fields.query.subject, 'Test query subject');
    I.fillField(this.fields.query.detail, 'Test query detail');
    I.click(this.fields.query.nonHearingRelated);
    I.click('Add new');
    I.see('Attach a document to this query');
    await I.click('Continue');
    I.waitForElement(this.fields.query.cya);
    await I.see('Query subject');
  },

  async enterHearingQueryDetails() {
    await I.runAccessibilityTest();
    I.waitForElement(this.fields.query.id);
    I.fillField(this.fields.query.subject, 'Test Hearing query subject');
    I.fillField(this.fields.query.detail, 'Test Hearing query detail');
    I.click(this.fields.query.hearingRelated);
    await date.enterDate(this.fields.query.hearingDate.id, 10);
    I.click('Add new');
    I.see('Attach a document to this query');
    await I.click('Continue');
    I.waitForElement(this.fields.query.cya);
    await I.see('Query subject');
  },
};
