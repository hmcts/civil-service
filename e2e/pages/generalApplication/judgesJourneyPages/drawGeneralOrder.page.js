const {I} = inject();
const {verifyJudgeRecitalText, selectCourtsOrderType} = require('../../generalAppCommons');

module.exports = {

  fields: {
    hearingDetailsJudgeRecitalTextArea: '#judicialGeneralHearingOrderRecital',
    hearingDetailsDirectionsTextArea: '#judicialGOHearingDirections',
    writtenRepresentationsJudgeRecitalTextArea: '#judgeRecitalText',
    writtenRepresentationsDirectionsTextArea: '#directionInRelationToHearingText',
    courtOrder: {
      coDateId: 'orderCourtOwnInitiativeDate',
      woDateId: 'orderWithoutNoticeDate',
      courtOrderText: 'textarea[id*="orderCourtOwnInitiative"]',
      wnOrderText: 'textarea[id*="orderWithoutNotice"]',
    }
  },

  async verifyHearingDetailsGeneralOrderScreen(hearingPreferences, timeEstimate, notice, orderType) {
    await I.waitForElement(this.fields.hearingDetailsJudgeRecitalTextArea);
    I.seeInCurrentUrl('/MAKE_DECISIONGAJudicialHearingDetailsGeneralOrderScreen');
    I.see('Draw a General Order');
    I.see('Judge’s recital');
    await verifyJudgeRecitalText(await I.grabValueFrom(this.fields.hearingDetailsJudgeRecitalTextArea), notice);
    await I.see(`The hearing will be held via ${hearingPreferences}.`);
    await I.see(`Estimated length of hearing is ${timeEstimate}`);
    await I.see('Directions in relation to hearing');

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

    await I.fillField(this.fields.hearingDetailsDirectionsTextArea, 'Test Directions');
    await I.clickContinue();
  },

  async verifyWrittenRepresentationsDrawGeneralOrderScreen(representationsType, notice, orderType) {
    await I.waitForElement(this.fields.writtenRepresentationsJudgeRecitalTextArea);
    I.seeInCurrentUrl('/MAKE_DECISIONGAJudicialWrittenRepresentationsDrawGeneralOrder');
    I.see('Draw a General Order');
    I.see('Judge’s recital');
    await verifyJudgeRecitalText(await I.grabValueFrom(this.fields.writtenRepresentationsJudgeRecitalTextArea), notice);
    if ('sequentialRep' === representationsType) {
      await I.see('The defendant should upload any written responses or evidence by 4pm on');
      await I.see('The claimant should upload any written responses or evidence in reply by 4pm on');
    } else {
      await I.see('The claimant and defendant should upload any written submissions and evidence by 4pm on');
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

    await I.fillField(this.fields.writtenRepresentationsDirectionsTextArea, 'Test Directions');
    await I.clickContinue();
  },
};


