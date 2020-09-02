const {I} = inject();

module.exports = {

  fields: {
    responseIntention: {
      id: '#respondent1ClaimResponseIntentionType',
      options: {
        fullDefence: 'Defend all of the claim',
        partDefence: 'Defend part of the claim',
        contestJurisdiction: 'Contest jurisdiction'
      }
    }
  },

  async selectResponseIntention() {
    I.waitForElement(this.fields.responseIntention.id);
    await within(this.fields.responseIntention.id, () => {
      I.click(this.fields.responseIntention.options.fullDefence);
    });
    await I.clickContinue();
  }
};

