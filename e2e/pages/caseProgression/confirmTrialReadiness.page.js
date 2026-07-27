const {I} = inject();
const config = require('./../../config');

module.exports = {
  fields: function (partyType){
    return {
        trialReady: {
          id: `#trialReady${partyType}`,
          options: {
            yes: `#trialReady${partyType}-Yes`,
            no: `#trialReady${partyType}-No`
          }
        },
        revisedHearingRequirements: {
          id: `#${partyType}RevisedHearingRequirements_revisedHearingRequirements_radio`,
          options: {
            yes: `#${partyType}RevisedHearingRequirements_revisedHearingRequirements_Yes`,
            no: `#${partyType}RevisedHearingRequirements_revisedHearingRequirements_No`
          }
        },
        revisedHearingComments: `#${partyType}RevisedHearingRequirements_revisedHearingComments`,
        hearingOtherComments: `#${partyType}HearingOtherComments_hearingOtherComments`
    };
  },

  async updateTrialConfirmation(user = config.applicantSolicitorUser, readyForTrial = 'yes', hearingRequirementsChanged = 'yes') {
      let trailReadyId, partyType;
      if (user == config.applicantSolicitorUser) {
        trailReadyId = 'Applicant';
        partyType = 'applicant';
      } else if (user == config.defendantSolicitorUser) {
        trailReadyId = 'Respondent1'; 
        partyType = 'respondent1';
      } else if (user == config.secondDefendantSolicitorUser) {
        trailReadyId = 'Respondent2'; 
        partyType = 'respondent2';
      }
      await I.waitForElement(this.fields(trailReadyId).trialReady.id);
      await I.runAccessibilityTest();
      if (readyForTrial == 'yes') {
        await I.click(this.fields(trailReadyId).trialReady.options.yes);
      } else {
        await I.click(this.fields(trailReadyId).trialReady.options.no);
      }
      await I.waitForElement(this.fields(partyType).revisedHearingRequirements.id);
      if (hearingRequirementsChanged == 'yes') {
        await I.click(this.fields(partyType).revisedHearingRequirements.options.yes);
        await I.waitForElement(this.fields((partyType)).revisedHearingComments);
        await I.fillField(this.fields(partyType).revisedHearingComments, 'Revised hearing comments');
      } else {
        await I.waitForElement(this.fields(partyType).revisedHearingRequirements.options.no);
      }
      await I.fillField(this.fields(partyType).hearingOtherComments, 'Court needs to know this info');
      await I.clickContinue();
  }
};
