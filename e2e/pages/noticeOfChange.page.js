const {I} = inject();
const config = require('../config.js');

module.exports = {

  fields: {
    caseIdField: '#caseRef',
    clientName: '#clientName',
    checkbox1: '#affirmation',
    checkbox2: '#notifyEveryParty',
  },

  async initiateNoticeOfChange() {
    await I.waitForText('Notice of change');
    await I.click('Notice of change');
    await I.amOnPage(config.url.manageCase + '/noc');
    await I.waitForText('Online case reference number', 60);
  },

  async enterCaseId(caseId) {
    I.waitForElement(this.fields.caseIdField);
    I.fillField(this.fields.caseIdField, caseId);
    await I.forceClick('Continue');
  },

  async enterClientName(clientName) {
    I.waitForElement(this.fields.clientName);
    I.fillField(this.fields.clientName, clientName);
    await I.forceClick('Continue');
  },

  async checkAndSubmit(caseId) {
    I.waitForText('You\'re satisfied that all these details are accurate and match what is written on the case');
    I.checkOption(this.fields.checkbox1);
    I.checkOption(this.fields.checkbox2);
    I.click('Submit');
    I.waitForText('Notice of change successful');
    I.waitForText('You\'re now representing a client on case');
    let caseIdWithDashes = caseId.toString().replace(/(.{4})/g,'$1-').substr(0, 19);
    I.waitForText(caseIdWithDashes);
    I.click('View this case');
    I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseId);
  }
};
