const {I} = inject();

module.exports = {

  fields: (respondentNumber) => {
    return {
      responseIntention: {
        id: respondentNumber ? `#respondent${respondentNumber}ClaimResponseIntentionType` : '#respondent1ClaimResponseIntentionTypeApplicant2',
        options: {
          fullDefence: `${respondentNumber ? `#respondent${respondentNumber}ClaimResponseIntentionType` : '#respondent1ClaimResponseIntentionTypeApplicant2'}-FULL_DEFENCE`,
          partDefence: `${respondentNumber ? `#respondent${respondentNumber}ClaimResponseIntentionType` : '#respondent1ClaimResponseIntentionTypeApplicant2'}-PART_DEFENCE`,
          contestJurisdiction: `${respondentNumber ? `#respondent${respondentNumber}ClaimResponseIntentionType` : '#respondent1ClaimResponseIntentionTypeApplicant2'}-CONTEST_JURISDICTION`
        }
      },
    };
  },

  async selectResponseIntention(respondent1Intention = 'fullDefence', respondent2Intention, respondent1Applicant2ClaimIntention) {
     
    if(respondent1Intention) {
      await I.click(this.fields(1).responseIntention.options[respondent1Intention]);
    }
    if(respondent2Intention) {
      await I.click(this.fields(2).responseIntention.options[respondent2Intention]);
    }
    if(respondent1Applicant2ClaimIntention) {
      await I.click(this.fields(null).responseIntention.options[respondent1Applicant2ClaimIntention]);
    }
    await I.clickContinue();
  },
};

