const { I } = inject();

module.exports = {

  fields: {
    jurisdiction: 'jurisdiction',
    caseType: 'case-type',
    event: 'event',
  },
  startButton: 'Start',

  async selectCaseType() {
      I.selectOption(this.fields.jurisdiction, 'Civil');
      I.selectOption(this.fields.caseType, 'Unspecified Claims');
      I.selectOption(this.fields.event, 'Create claim');
      await I.click(this.startButton);
  }
};

