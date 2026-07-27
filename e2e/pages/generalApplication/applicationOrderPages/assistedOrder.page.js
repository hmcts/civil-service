const {expect} = require('chai');
const date = require('../../../fragments/date');
const {selectCourtsOrderType} = require('../../generalAppCommons');
const {I} = inject();

module.exports = {

  fields: {
    orderMade: {
      id: '#assistedOrderMadeSelection',
      options: {
        yes: '#assistedOrderMadeSelection_Yes',
        no: '#assistedOrderMadeSelection_No'
      },
      dateId: 'singleDateHeard',
      singleDate: 'input[id*="SINGLE_DATE"]',
    },
    judgeHeardFrom: {
      judgeHeardFromCheckBox: '#assistedOrderJudgeHeardFrom-SHOW',
      repTypeCAndD: 'input[id*="CLAIMANT_AND_DEFENDANT"]',
      repTypeOther: 'input[id*="OTHER_REPRESENTATION"]',
      judgeConsideredPapers: 'input[id*="CONSIDERED"]',
      claimantDropDown: 'select[id*="claimantDefendantRepresentation_claimantRepresentation"]',
      defendantDropDown: 'select[id*="claimantDefendantRepresentation_defendantRepresentation"]',
      repTypeOtherTextArea: 'textarea[id*="otherRepresentation_detailsRepresentationText"]',
    },
    recitals: {
      recitalsShowCheckBox: '#assistedOrderRecitals-SHOW',
      recitalsRecordedTextArea: '#assistedOrderRecitalsRecorded_text',
    },
    costs: {
      defCostBase: 'input[id*="assistedCostTypes-COSTS_RESERVED"]',
      costReserved: 'textarea[id*="costReservedDetails_detailsRepresentationText"]',
      isPartyCostProtection: 'input[id="publicFundingCostsProtection_Yes"]',
    },
    furtherHearing: {
      furtherHearingShowCheckBox: '#assistedOrderFurtherHearingToggle-SHOW',
      listFromDateId: 'listFromDate',
      hearingLength: 'input[id*="HOURS_2"]',
      hearingMethod: 'input[id*="VIDEO"]',
      datesToAvoidYesNo: 'input[id="assistedOrderFurtherHearingDetails_datesToAvoidYesNo_No"]',
    },
    appeal: {
      appealShowCheckBox: '#assistedOrderAppealToggle-SHOW',
      appealOrigin: 'input[id*="appealOrigin-CLAIMANT"]',
      appealDecision: 'input[id*="permissionToAppeal-GRANTED"]',
      appealJudgeSelection: 'select[id*="assistedOrderAppealJudgeSelection"]',
    },
    courtsOrder: {
      id: '#orderMadeOnOption',
      options: {
        courtOwnInitiativeOrder: 'Order on court\'s own initiative',
        withoutNoticeOrder: 'Order without notice',
        noneOrder: 'None',
      },
      initiativeOrderText: 'textarea[id*="orderMadeOnOwnInitiative_detailText"]',
      withoutNoticeOrderText: 'textarea[id*="orderMadeOnWithOutNotice_detailText"]',
    },
    reasons: {
      reasonType: 'input[id="assistedOrderGiveReasonsYesNo_Yes"]',
      reasonsText: 'textarea[id*="assistedOrderGiveReasonsDetails_reasonsText"]',
    }
  },

  async isOrderMade(orderMade, orderType) {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFinalOrderAssistedOrder', 5);
    await I.see('Test Inc v Sir John Doe, Dr Foo Bar');
    await I.see('Order Made');
    await I.see('Is this order made following a hearing?');

    I.waitForElement(this.fields.orderMade.id);
    await within(this.fields.orderMade.id, () => {
      I.click(this.fields.orderMade.options[orderMade]);
    });
    await I.see('Enter date(s) of hearing');
    await I.forceClick(this.fields.orderMade.singleDate);
    await date.verifyPrePopulatedDate(this.fields.orderMade.dateId, orderType);
  },

  async fillJudgeHeardForm() {
    await I.see('Judge heard from');
    await I.dontSeeCheckboxIsChecked(this.fields.judgeHeardFrom.judgeHeardFromCheckBox);
    await I.click(this.fields.judgeHeardFrom.judgeHeardFromCheckBox);
    await I.waitForText('Judge considered the papers');
    await I.dontSeeCheckboxIsChecked(this.fields.judgeHeardFrom.repTypeCAndD);
    await I.dontSeeCheckboxIsChecked(this.fields.judgeHeardFrom.repTypeOther);
    await I.dontSeeCheckboxIsChecked(this.fields.judgeHeardFrom.judgeConsideredPapers);

    await I.click(this.fields.judgeHeardFrom.repTypeCAndD);
    await I.seeNumberOfElements(this.fields.judgeHeardFrom.claimantDropDown, 1);
    await I.seeNumberOfElements(this.fields.judgeHeardFrom.defendantDropDown, 1);

    await I.click(this.fields.judgeHeardFrom.repTypeOther);
    await I.see('Enter details of representation');
    await I.fillField(this.fields.judgeHeardFrom.repTypeOtherTextArea, 'Test Other rep');
    await I.click(this.fields.judgeHeardFrom.judgeConsideredPapers);
  },

  async fillRecitals() {
    await I.see('Recitals');
    await I.dontSeeCheckboxIsChecked(this.fields.recitals.recitalsShowCheckBox);
    await I.click(this.fields.recitals.recitalsShowCheckBox);
    await I.waitForText('It is recorded that:');
    await I.fillField(this.fields.recitals.recitalsRecordedTextArea, 'Test recital records');
    await I.see('It is ordered that:');
    let orderDetails = await I.grabValueFrom('#assistedOrderOrderedThatText');
    await expect(orderDetails).to.equals('Test Order details');
  },

  async selectCosts() {
    await I.see('Costs');
    await I.click(this.fields.costs.defCostBase);
    await I.waitForText('Costs reserved');
    await I.fillField(this.fields.costs.costReserved, 'to the hearing judge');
    await I.click(this.fields.costs.isPartyCostProtection);
    await I.see('Does the paying party have public funding costs protection?');
  },

  async selectFurtherHearing() {
    await I.see('Further hearing');
    await I.dontSeeCheckboxIsChecked(this.fields.furtherHearing.furtherHearingShowCheckBox);
    await I.click(this.fields.furtherHearing.furtherHearingShowCheckBox);
    await I.waitForText('Length of new hearing');
    await I.see('Date of new hearing');
    await date.enterDate(this.fields.furtherHearing.listFromDateId, +1);
    await I.click(this.fields.furtherHearing.hearingLength);
    await I.click(this.fields.furtherHearing.hearingMethod);
  },

  async selectAppeal() {
    await I.see('Appeal');
    await I.dontSeeCheckboxIsChecked(this.fields.appeal.appealShowCheckBox);
    await I.click(this.fields.appeal.appealShowCheckBox);
    await I.waitForText('Reasons');
    await I.click(this.fields.appeal.appealOrigin);
    await I.click(this.fields.appeal.appealDecision);
    await I.selectOption(this.fields.appeal.appealJudgeSelection, 'a Circuit Judge of the County Court');
  },

  async selectOrderType(order) {
    switch (order) {
      case 'courtOwnInitiativeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtsOrder.initiativeOrderText)).trim(), order);
        break;
      case 'withoutNoticeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtsOrder.withoutNoticeOrderText)).trim(), order);
        break;
      case 'noneOrder':
        await selectCourtsOrderType('', order, '');
        break;
    }
  },

  async selectReasons() {
    await I.see('Reasons');
    await I.click(this.fields.reasons.reasonType);
    await I.waitForText('Brief reasons');
    await I.fillField(this.fields.reasons.reasonsText, 'Test reasons ...');
    await I.clickContinue();
  },

  async verifyAssistedOrderErrorMessage() {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFinalOrderAssistedOrder', 5);
    await I.click('Continue');
    await I.seeNumberOfVisibleElements('.error-message', 3);
  },
};

