const {I} = inject();

module.exports = {

  fields: {
    jurisdiction: 'jurisdiction',
    caseType: 'case-type',
    event: 'event',
  },
  startButton: 'Start',

  async createCase(jurisdiction) {
    await I.retryUntilExists( () => {
      I.retry(3).click('Create case');
    }, `#cc-jurisdiction > option[value="${jurisdiction}"]`);

    await I.retryUntilExists(() => {
      I.selectOption(this.fields.jurisdiction, 'Civil');
      I.selectOption(this.fields.caseType, 'Civil');
      I.selectOption(this.fields.event, 'Create claim');
      I.click(this.startButton);
    }, 'ccd-markdown');
  }
};

