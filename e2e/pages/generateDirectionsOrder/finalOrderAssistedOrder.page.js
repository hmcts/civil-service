const {I} = inject();

module.exports = {
  fields: {
    finalOrderMadeSelection: {
      id: '#finalOrderMadeSelection',
      options: {
        yes: '#finalOrderMadeSelection_Yes',
        no: '#finalOrderMadeSelection_No'
      }
    },
    finalOrderDateHeardComplex: {
      id: '#finalOrderDateHeardComplex',
      options: {
        singleDate: {
          id: '#finalOrderDateHeardComplex_finalOrderMadeRadioList-SINGLE_DATE',
          day: '#singleDate-day',
          month: '#singleDate-month',
          year: '#singleDate-year'
        },
        dateRange: '#finalOrderDateHeardComplex_finalOrderMadeRadioList-DATE_RANGE',
        bespokeRange: '#finalOrderDateHeardComplex_finalOrderMadeRadioList-BESPOKE_RANGE'
      },
    },
    finalOrderJudgePapers: {
      id: '#finalOrderJudgePapers-CONSIDERED',
    },
    finalOrderJudgeHeardFromShow: {
      id: '#finalOrderJudgeHeardFrom-SHOW',
    },
    finalOrderRepresentation: {
      id: '#finalOrderRepresentation',
      options: {
        claimantAndDefendant: '#finalOrderRepresentation_typeRepresentationList-CLAIMANT_AND_DEFENDANT',
        otherRepresentation: '#finalOrderRepresentation_typeRepresentationList-OTHER_REPRESENTATION',
      }
    },
    recitalsShow: {
      id: '#finalOrderRecitals-SHOW',
    },
    recitals: {
      id: '#finalOrderRecitalsRecorded_text'
    },
    finalOrderText: {
      id: '#finalOrderOrderedThatText',
    },
    costs: {
      id: '#assistedOrderCostList',
      options: {
        costsInCase: '#assistedOrderCostList-COSTS_IN_THE_CASE',
        noOrderToCost: '#assistedOrderCostList-NO_ORDER_TO_COST',
        costsReserved: '#assistedOrderCostList-COSTS_RESERVED',
        detailedCosts: '#assistedOrderCostList-MAKE_AN_ORDER_FOR_DETAILED_COSTS',
        bespokeCosts: '#assistedOrderCostList-BESPOKE_COSTS_ORDER',
      }
    },
    hearingNotes: {
      id: '#freeFormHearingNotes'
    },
    orderMadeOnDetailsList: {
      id: '#orderMadeOnDetailsList',
      options: {
        courtsInitiative: '#orderMadeOnDetailsList-COURTS_INITIATIVE',
        withoutNotice: '#orderMadeOnDetailsList-WITHOUT_NOTICE',
        none: '#orderMadeOnDetailsList-NONE',
      }
    },
    finalOrderReasons: {
      id: '#finalOrderGiveReasonsYesNo',
      options: {
        yes: '#finalOrderGiveReasonsYesNo_Yes',
        no: '#finalOrderGiveReasonsYesNo_No'
      }
    }
  },

  async enterOrderDetails() {
    await I.waitForText('Order Made');
    await I.runAccessibilityTest();
    await I.click(this.fields.finalOrderMadeSelection.options.yes);
    await I.click(this.fields.finalOrderDateHeardComplex.options.singleDate.id);
    await I.click(this.fields.finalOrderJudgePapers.id);
    await I.click(this.fields.recitalsShow.id);
    await I.fillField(this.fields.finalOrderText.id, 'The court orders that...');
    await I.click(this.fields.costs.options.costsInCase);
    await I.click(this.fields.orderMadeOnDetailsList.options.courtsInitiative);
    await I.click(this.fields.finalOrderReasons.options.no);
    await I.clickContinue();
  },
};