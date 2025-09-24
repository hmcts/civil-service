const {I} = inject();

module.exports = {

  fields: {
    defendantDefaultJudgmentOptions: {
      id: '#defendantDetailsSpec',
      options: {
        defendantname: '#defendantDetailsSpec > fieldset > div',
        both: '#defendantDetailsSpec > fieldset > div:nth-of-type(3)'
      }
    },

    statementsApplyForDJ:{
      options: {
        ONE_V_ONE: '#CPRAcceptance_acceptance-CERTIFIED',
        ONE_V_TWO: '#CPRAcceptance2Def_acceptance-CERTIFIED'
      }
    },

    defendantPartialPayment:{
      id: '#paymentConfirmationDecisionSpec_radio',
      options: {
        yes: '#partialPayment_Yes',
        no: '#partialPayment_No'
      }
    },
    claimForFixedCostsOnEntry:{
      id: '#claimFixedCostsOnEntryDJ_radio',
      options: {
        yes: '#claimFixedCostsOnEntryDJ_Yes',
        no: '#claimFixedCostsOnEntryDJ_No'
      }
    },
    claimForFixedCosts:{
      id: '#partialPayment',
      options: {
        yes: '#paymentConfirmationDecisionSpec_Yes',
        no: '#paymentConfirmationDecisionSpec_No'
      }
    },

    paymentTypeSelection:{
      id: '#paymentTypeSelection',
      options: {
        immediately: '#paymentTypeSelection-IMMEDIATELY',
        setDate: '#paymentTypeSelection-SET_DATE',
        repaymentPlan: '#paymentTypeSelection-REPAYMENT_PLAN',
      }
    },
  },

  async againstWhichDefendant(scenario){
    if(scenario==='ONE_V_ONE'){
      await within(this.fields.defendantDefaultJudgmentOptions.id, () => {
        I.click(this.fields.defendantDefaultJudgmentOptions.options.defendantname);
      });
    }else if (scenario==='ONE_V_TWO'){
      await within(this.fields.defendantDefaultJudgmentOptions.id, () => {
        I.click(this.fields.defendantDefaultJudgmentOptions.options.both);
      });
    }
    await I.clickContinue();
  },

  async hasDefendantMadePartialPayment(){
    await I.click(this.fields.defendantPartialPayment.options.no);
    await I.clickContinue();
  },

  async claimForFixedCosts(){
    await I.click(this.fields.claimForFixedCosts.options.no);
    await I.clickContinue();
  },

  async claimForFixedCostsOnEntry(){
    await I.click(this.fields.claimForFixedCostsOnEntry.options.no);
    await I.clickContinue();
  },

  async repaymentSummary(){
    await I.clickContinue();
  },

  async paymentTypeSelection(){
    await I.click(this.fields.paymentTypeSelection.options.immediately);
    await I.clickContinue();
  },

  async statementToCertify(scenario) {
    if(scenario==='ONE_V_ONE'){
      await I.click(this.fields.statementsApplyForDJ.options.ONE_V_ONE);
    }else if (scenario==='ONE_V_TWO'){
      await I.click(this.fields.statementsApplyForDJ.options.ONE_V_TWO);
    }
    await I.clickContinue();
  }
};
