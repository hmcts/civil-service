const {I} = inject();
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');
const config = require('../config');

const EVENT_TRIGGER_LOCATOR = 'ccd-case-event-trigger';
const EVENT_QM_LOCATOR = 'div.query-form-container';

module.exports = {

  components: {
    caseFlags: 'ccd-field-read'
  },
  tabs: {
    history: 'History',
    caseFlags: 'Case Flags'
  },
  fields: {
    eventDropdown: '#next-step',
    tabButton: 'div.mat-tab-label-content',
    dayOfPermission: '#permissionGrantedDate-day',
    monthOfPermission: '#permissionGrantedDate-month',
    yearOfPermission: '#permissionGrantedDate-year',
    judgeName: '#permissionGrantedComplex_permissionGrantedJudge',
    caseNote: '#caseNote',
    judgeOrder: '#settleReason-JUDGE_ORDER',
    signOutLink: 'ul[class*="navigation-list"] a',
  },
  goButton: '.button[type="submit"]',

  start: async function (event) {
    await I.waitForElement(this.fields.eventDropdown, 90);
    await I.selectOption(this.fields.eventDropdown, event);
    await I.forceClick(this.goButton);
  },

  async startEvent(event, caseId) {
    await waitForFinishedBusinessProcess(caseId);
    await I.retryUntilExists(async() => {
      await I.navigateToCaseDetails(caseId);
      // await this.start(event);
      await I.amOnPage(`${config.url.manageCase}/cases/case-details/${caseId}/trigger/${event.id}/${event.id}`);
    }, EVENT_TRIGGER_LOCATOR, 3, 45);
  },
  
  async raiseNewQuery(caseId) {
    await I.retryUntilExists(async() => {
      await I.navigateToCaseDetails(caseId);
      await I.amOnPage(`${config.url.manageCase}/query-management/query/${caseId}`);
    }, EVENT_QM_LOCATOR, 3, 45);
  },

  async permissionGrantedByJudge() {
    await I.runAccessibilityTest();
    I.fillField(this.fields.judgeName, 'Testing');
    I.fillField(this.fields.dayOfPermission, 29);
    I.fillField(this.fields.monthOfPermission, 8);
    I.fillField(this.fields.yearOfPermission, 2024);
    await I.clickContinue();
  },
  async selectJudgeOrder() {
    await I.runAccessibilityTest();
    I.click(this.fields.judgeOrder);
    await I.clickContinue();
  },

  async caseNoteForClaimDiscontinuedRemoveHearing() {
    await I.runAccessibilityTest();
    I.fillField(this.fields.caseNote, 'Testing');
    await I.clickContinue();
  },

  async verifyErrorMessageOnEvent(event, caseId, errorMsg) {
    await waitForFinishedBusinessProcess(caseId);
    await I.retryUntilExists(async() => {
    await I.navigateToCaseDetails(caseId);
    await I.selectOption(this.fields.eventDropdown, event);
   // await I.moveCursorTo(this.goButton);
    await I.wait(15);
    await I.forceClick(this.goButton);
    await I.waitForText(errorMsg);
  }, locate('#errors'));
},

  async navigateToTab(caseNumber, tabName) {
    await I.amOnPage(config.url.manageCase + '/cases/case-details/' + caseNumber);
    await I.waitForSelector(this.fields.signOutLink, 30);
    await I.retryUntilExists(() => I.clickTab(tabName), `//div[@aria-selected="true" and contains(., "${tabName}")]`);
  },

  async assertNoEventsAvailable() {
    if (await I.hasSelector(this.fields.eventDropdown)) {
      throw new Error('Expected to have no events available');
    }
  },

  async rejectCookieBanner() {
    if (await I.see('We use some essential cookies to make this service work.')) {
      await I.click('Reject analytics cookies');
      await I.wait(5);
    }
  },

  async assertEventsAvailable(events) {
    await I.waitForElement(this.fields.eventDropdown);
    events.forEach(event => I.see(event, this.fields.eventDropdown));
  },

  async goToCaseFlagsTab(caseId) {
    await I.navigateToCaseFlags(caseId);
    await I.waitForElement(this.components.caseFlags);
  },

  async assertCaseFlagsInfo(numberOfFlags) {
    I.see(`There ${numberOfFlags > 1 ? 'are' : 'is'} ${numberOfFlags} active flag${numberOfFlags > 1 ? 's' : ''} on this case.`);
  },

  async assertCaseFlags(caseFlags) {
    console.log('validating case flags');
    caseFlags.forEach(({partyName, details}) => {
      console.log(`Verifying party name [${partyName}] is displayed`);
      I.see(partyName, this.components.caseFlags);
      details.forEach(({name}) => {
        console.log(`Verifying [${name}] flag is displayed`);
        I.see(name, this.components.caseFlags);
      });
    });
  },

  async assertInactiveCaseFlagsInfo(numberOfFlags) {
    console.log('Verifying active case flags banner is not visible.');
    I.dontSee(`There ${numberOfFlags > 1 ? 'are' : 'is'} ${numberOfFlags} active flag${numberOfFlags > 1 ? 's' : ''} on this case.`);
  },

  async assertUpdatedCaseFlags(caseFlags) {
    console.log('validating updated case flags');
    caseFlags.forEach(({partyName, flagComment}) => {
      console.log('Verifying updated flag comment is displayed');
      I.see(`${flagComment} - Updated - ${partyName}`, this.components.caseFlags);
    });
  }
};
