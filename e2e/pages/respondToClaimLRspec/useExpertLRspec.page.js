const {I} = inject();
module.exports = {
  fields: function(mpScenario) {
    switch (mpScenario) {
      case 'ClaimantResponse': {
        return {
          useExpert: {
            id: '#applicant1ClaimExpertSpecRequired_radio',
            options: {
              yes: '#applicant1ClaimExpertSpecRequired_Yes',
              no: '#applicant1ClaimExpertSpecRequired_No'
            }
          },
        };
      }

      case 'DefendantResponse':
      default: {
        return {
          useExpert: {
            id: '#responseClaimExpertSpecRequired_radio',
            options: {
              yes: '#responseClaimExpertSpecRequired_Yes',
              no: '#responseClaimExpertSpecRequired_No'
            },
            expertName: '#respondToClaim_experts_expertName',
            expertField: '#respondToClaim_experts_fieldofExpertise',
            cost: '#respondToClaim_experts_estimatedCost',
          }
        };
      }
    }
  },


 async claimExpert(mpScenario) {

    I.waitForElement(this.fields(mpScenario).useExpert.id);
    await I.runAccessibilityTest();
    await within(this.fields(mpScenario).useExpert.id, () => {
    I.click(this.fields(mpScenario).useExpert.options.no);
    });

    await I.clickContinue();
  }
};