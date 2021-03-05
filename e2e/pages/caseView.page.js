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
    I.selectOption(this.fields.eventDropdown, event);
    I.click(this.goButton);
    I.waitForElement(EVENT_TRIGGER_LOCATOR);
  },

  async assertNoEventsAvailable() {
    if (await I.hasSelector(this.fields.eventDropdown)) {
      throw new Error('Expected to have no events available');
    }
  }
};
