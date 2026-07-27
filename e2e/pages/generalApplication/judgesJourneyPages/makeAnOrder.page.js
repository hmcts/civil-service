/* eslint-disable  no-case-declarations */
const {I} = inject();
const expect = require('chai').expect;
const {verifyJudgeRecitalText, selectCourtsOrderType} = require('../../generalAppCommons');
const date = require('../../../fragments/date');

module.exports = {

  fields: {
    makeAnOrder: {
      id: '#judicialDecisionMakeOrder_makeAnOrder',
      options: {
        approveOrEditTheOrder: 'Approve or edit the order requested by the applicant',
        dismissTheApplication: 'Dismiss the application',
        giveDirections: 'Give directions without listing for hearing'
      }
    },
    courtOrder: {
      coDateId: 'orderCourtOwnInitiativeDate',
      woDateId: 'orderWithoutNoticeDate',
      courtOrderText: 'textarea[id*="orderCourtOwnInitiative"]',
      wnOrderText: 'textarea[id*="orderWithoutNotice"]',
    },
    judgeRecitalTextArea: '#judicialDecisionMakeOrder_judgeRecitalText',
    orderTextArea: '#judicialDecisionMakeOrder_orderText',
    dismissalOrderTextArea: '#judicialDecisionMakeOrder_dismissalOrderText',
    directionsTextArea: '#judicialDecisionMakeOrder_directionsText',
    showReasonForDecisionTextArea: {
      id: '#judicialDecisionMakeOrder_showReasonForDecision_radio',
      options: {
        yes: '#judicialDecisionMakeOrder_showReasonForDecision_Yes',
        no: '#judicialDecisionMakeOrder_showReasonForDecision_No'
      }
    },
    reasonForDecisionTextArea: '#judicialDecisionMakeOrder_reasonForDecisionText',
    consentAgreementCheckBox: '#makeAppVisibleToRespondents_makeAppAvailableCheck-CONSENT_AGREEMENT_CHECKBOX',
    directionsResponseDate: 'directionsResponseByDate',
    documentDropdown: '#judicialDecisionMakeOrder_judgeApproveEditOptionDoc',
    judgeApproveEditOptionDateDay: '#judgeApproveEditOptionDate-day',
    judgeApproveEditOptionDateMonth: '#judgeApproveEditOptionDate-month',
    judgeApproveEditOptionDateYear: '#judgeApproveEditOptionDate-year',
  },

  async selectAnOrder(order, notice, orderType) {
    await I.waitForElement(this.fields.makeAnOrder.id);
    I.seeInCurrentUrl('/MAKE_DECISIONGAJudicialMakeADecisionScreen');
    I.see('The court records that:');
    await verifyJudgeRecitalText(await I.grabValueFrom(this.fields.judgeRecitalTextArea), notice);
    I.see('Reasons');
    if (notice === 'no') {
      I.seeTextEquals('This application is cloaked', '#applicationIsCloakedLabel h2');
      I.see('Make application visible to all parties');
      I.click(this.fields.consentAgreementCheckBox);
    }
    await within(this.fields.makeAnOrder.id, () => {
      I.click(this.fields.makeAnOrder.options[order]);
    });
    switch (order) {
      case 'approveOrEditTheOrder':
        let orderText = await I.grabValueFrom(this.fields.orderTextArea);
        expect(orderText).to.contains('Test Order details');
        break;
      case 'dismissTheApplication':
        I.fillField(this.fields.dismissalOrderTextArea, 'Judges dismissed the order');
        break;
      case 'giveDirections':
        I.fillField(this.fields.directionsTextArea, 'Judges directions');
        I.see('When should this application be referred to a Judge again?');
        await date.enterDate(this.fields.directionsResponseDate, +2);
        break;
    }

    switch (orderType) {
      case 'courtOwnInitiativeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtOrder.courtOrderText)).trim(),
          orderType, this.fields.courtOrder.coDateId);
        break;
      case 'withoutNoticeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtOrder.wnOrderText)).trim(),
          orderType, this.fields.courtOrder.woDateId);
        break;
      case 'noneOrder':
        await selectCourtsOrderType('', orderType, '');
        break;
    }
    await within(this.fields.showReasonForDecisionTextArea.id, () => {
      I.click(this.fields.showReasonForDecisionTextArea.options.yes);
    });
    await I.fillField(this.fields.reasonForDecisionTextArea, 'Judges Decision');
    await I.clickContinue();
  }
};
