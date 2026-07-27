const {I} = inject();

module.exports = {
  fields: function(mpScenario) {
    switch (mpScenario) {
      case 'ONE_V_TWO_ONE_LEGAL_REP':
      case 'ONE_V_TWO_TWO_LEGAL_REP': {
        return {
          proceed: {
            id: '#applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2',
            options: {
              yes: '#applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2_Yes',
              no: '#applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2_No'
            }
          },
          proceedForSecondPerson: {
            id: '#applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2',
            options: {
              yes: '#applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2_Yes',
              no: '#applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2_No'
            }
          }
        };
      }
      case 'TWO_V_ONE': {
        return {
          proceed: {
            id: '#applicant1ProceedWithClaimMultiParty2v1',
            options: {
              yes: '#applicant1ProceedWithClaimMultiParty2v1_Yes',
              no: '#applicant1ProceedWithClaimMultiParty2v1_No'
            }
          },
          proceedForSecondPerson: {
            id: '#applicant2ProceedWithClaimMultiParty2v1',
            options: {
              yes: '#applicant2ProceedWithClaimMultiParty2v1_Yes',
              no: '#applicant2ProceedWithClaimMultiParty2v1_No'
            }
          }
        };
      }
      case 'ONE_V_ONE':
      default: {
        return {
          proceed: {
            id: '#applicant1ProceedWithClaim',
            options: {
              yes: '#applicant1ProceedWithClaim_Yes',
              no: '#applicant1ProceedWithClaim_No'
            }
          }
        };
      }
    }
  },

  async proceedWithClaim(mpScenario) {
    await I.waitForElement(this.fields(mpScenario).proceed.id);
    await I.runAccessibilityTest();
    await within(this.fields(mpScenario).proceed.id, () => {
      I.click(this.fields(mpScenario).proceed.options.yes);
    });

    if(mpScenario !== 'ONE_V_ONE'){
      await within(this.fields(mpScenario).proceedForSecondPerson.id, () => {
        I.click(this.fields(mpScenario).proceedForSecondPerson.options.yes);
      });
    }
    await I.clickContinue();
  },

  async dropClaim(mpScenario) {
    I.waitForElement(this.fields(mpScenario).proceed.id);
    await I.runAccessibilityTest();
    await within(this.fields(mpScenario).proceed.id, () => {
      I.click(this.fields(mpScenario).proceed.options.no);
    });
    await I.clickContinue();
  }
};
