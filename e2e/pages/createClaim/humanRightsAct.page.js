const { I } = inject();

module.exports = {

  fields: {
    isHumanRightsActIssuesAdded: {
      id: '#isHumanRightsActIssues',
      options: {
        yes: '#isHumanRightsActIssues_Yes',
        no: '#isHumanRightsActIssues_No'
      }
    }
  },

  async humanRightsAct(option) {
    I.waitForElement(this.fields.isHumanRightsActIssuesAdded.id);
    if(option === 'yes') {
      await within(this.fields.isHumanRightsActIssuesAdded.id, () => {
        I.click(this.fields.isHumanRightsActIssuesAdded.options.yes);
      });
    } else {
      await within(this.fields.isHumanRightsActIssuesAdded.id, () => {
        I.click(this.fields.isHumanRightsActIssuesAdded.options.no);
      });
    }
    await I.clickContinue();
  }
};
