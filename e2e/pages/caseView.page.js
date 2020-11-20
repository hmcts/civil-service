const {I} = inject();
const {waitForFinishedBusinessProcess} = require('../api/testingSupport');

const EVENT_TRIGGER_LOCATOR = 'ccd-case-event-trigger';

module.exports = {

  tabs: {
    history: 'History'
  },
  fields: {
    eventDropdown: '#next-step',
  },
  goButton: 'Go',

  async startEvent(event, caseId) {
    await waitForFinishedBusinessProcess(caseId);
    await I.goToCase(caseId);
    I.selectOption(this.fields.eventDropdown, event);
    I.click(this.goButton);
    I.waitForElement(EVENT_TRIGGER_LOCATOR);
  }
};
