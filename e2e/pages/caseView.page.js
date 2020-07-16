const {I} = inject();

module.exports = {

  tabs: {
    history: 'History'
  },
  fields: {
    eventDropdown: '#next-step',
  },
  goButton: 'Go',

  async startEvent(event) {
    await I.retryUntilExists(() => {
      I.selectOption(this.fields.eventDropdown, event);
      I.click(this.goButton);
    }, 'ccd-case-event-trigger');
  }
};
