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

  async selectResponseIntention(responseIntention) {
    // eslint-disable-next-line no-prototype-builtins
    if (!this.fields.responseIntention.options.hasOwnProperty(responseIntention)) {
      throw new Error(`Response intention: ${responseIntention} does not exist`);
    }
    I.waitForElement(this.fields.responseIntention.id);
    await I.runAccessibilityTest();
    await within(this.fields.responseIntention.id, () => {
      I.click(this.fields.responseIntention.options[responseIntention]);
    });
    await I.clickContinue();
  }
};

