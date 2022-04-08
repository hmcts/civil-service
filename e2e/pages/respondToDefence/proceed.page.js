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
              yes: 'Yes',
              no: 'No'
            }
          },
          proceedForSecondPerson: {
            id: '#applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2',
            options: {
              yes: 'Yes',
              no: 'No'
            }
          }
        };
      }
      case 'TWO_V_ONE': {
        return {
          proceed: {
            id: '#applicant1ProceedWithClaimMultiParty2v1',
            options: {
              yes: 'Yes',
              no: 'No'
            }
          },
          proceedForSecondPerson: {
            id: '#applicant2ProceedWithClaimMultiParty2v1',
            options: {
              yes: 'Yes',
              no: 'No'
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
              yes: 'Yes',
              no: 'No'
            }
          }
        };
      }
    }
  },

  async proceedWithClaim(mpScenario) {
    I.waitForElement(this.fields(mpScenario).proceed.id);
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

  async dropClaim() {
    I.waitForElement(this.fields.proceed.id);
    await I.runAccessibilityTest();
    await within(this.fields.proceed.id, () => {
      I.click(this.fields.proceed.options.no);
    });
    await I.clickContinue();
  }
};
