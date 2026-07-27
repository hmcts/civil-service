const {I} = inject();
module.exports = {
  fields: function(mpScenario) {
    switch (mpScenario) {
      case 'ClaimantResponse': {
        return {
          mediationType: {
            id: '#applicant1ClaimMediationSpecRequiredLip_hasAgreedFreeMediation',
            options: {
              yes: '#applicant1ClaimMediationSpecRequiredLip_hasAgreedFreeMediation-Yes',
              no: '#applicant1ClaimMediationSpecRequiredLip_hasAgreedFreeMediation-No'
            }
          },
        };
      }

      case 'DefendantResponse':
      default: {
        return {
          mediationType: {
            id: '#responseClaimMediationSpecRequired_radio',
            options: {
              yes: '#responseClaimMediationSpecRequired_Yes',
              no: '#responseClaimMediationSpecRequired_No'
            }
          }
        };
      }
    }
  },


 async selectMediation(mpScenario) {

    I.waitForElement(this.fields(mpScenario).mediationType.id);
    await I.runAccessibilityTest();
    I.click(this.fields(mpScenario).mediationType.options.yes);

    await I.clickContinue();
  }
};
