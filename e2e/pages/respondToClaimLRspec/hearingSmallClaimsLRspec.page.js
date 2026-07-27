const {I} = inject();
module.exports = {
  fields: function(mpScenario) {
    switch (mpScenario) {
      case 'ClaimantResponse': {
        return {
          hearingType: {
            id: '#applicant1DQSmallClaimHearing_unavailableDatesRequired_radio',
            options: {
              yes: '#applicant1DQSmallClaimHearing_unavailableDatesRequired_Yes',
              no: '#applicant1DQSmallClaimHearing_unavailableDatesRequired_No'
            }
          },
        };
      }

      case 'DefendantResponse':
      default: {
        return {
          hearingType: {
            id: '#SmallClaimHearingInterpreterRequired_radio',
            options: {
              yes: '#SmallClaimHearingInterpreterRequired_Yes',
              no: '#SmallClaimHearingInterpreterRequired_No'
            },
            id2: '#respondent1DQHearingSmallClaim_unavailableDatesRequired_radio',
            id2options: {
              yes: '#respondent1DQHearingSmallClaim_unavailableDatesRequired_Yes',
              no: '#respondent1DQHearingSmallClaim_unavailableDatesRequired_No'
            }
          }
        };
      }
    }
  },

 async selectHearing(mpScenario) {

    I.waitForElement(this.fields(mpScenario).hearingType.id);
    await I.runAccessibilityTest();
    await within(this.fields(mpScenario).hearingType.id, () => {
    I.click(this.fields(mpScenario).hearingType.options.no);
    });

    if(mpScenario === 'DefendantResponse'){
          I.waitForElement(this.fields(mpScenario).hearingType.id2);
          await I.runAccessibilityTest();
          await within(this.fields(mpScenario).hearingType.id2, () => {
          I.click(this.fields(mpScenario).hearingType.id2options.no);
      });
    }

    await I.clickContinue();
  }
};
