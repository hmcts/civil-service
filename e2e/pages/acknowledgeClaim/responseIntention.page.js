const {I} = inject();

module.exports = {

  fields: (respondentNumber) => {
    return {
      responseIntention: {
        id: respondentNumber ? `#respondent${respondentNumber}ClaimResponseIntentionType` : '#respondent1ClaimResponseIntentionTypeApplicant2',
        options: {
          fullDefence: 'Defend all of the claim',
          partDefence: 'Defend part of the claim',
          contestJurisdiction: 'Contest jurisdiction'
        }
      },
    };
  },

  async selectResponseIntention(respondent1Intention = 'fullDefence', respondent2Intention, respondent1Applicant2ClaimIntention) {
    // eslint-disable-next-line no-prototype-builtins
    if(respondent1Intention) {
      await this.selectResponse('1', respondent1Intention);
    }
    if(respondent2Intention) {
      await this.selectResponse('2', respondent2Intention);
    }
    if(respondent1Applicant2ClaimIntention) {
      await this.selectResponse(null, respondent1Applicant2ClaimIntention);
    }
    await I.clickContinue();
  },

  async selectResponse(respondentNumber, responseIntention) {
    // eslint-disable-next-line no-prototype-builtins
    if (!this.fields(respondentNumber).responseIntention.options.hasOwnProperty(responseIntention)) {
      throw new Error(`Response intention: ${responseIntention} does not exist`);
    }
    I.waitForElement(this.fields(respondentNumber).responseIntention.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondentNumber).responseIntention.id, () => {
      I.click(this.fields(respondentNumber).responseIntention.options[responseIntention]);
    });
  }
};

