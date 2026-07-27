const date = require('../../../fragments/date');
const {selectCourtsOrderType} = require('../../generalAppCommons');
const {I} = inject();

module.exports = {

  fields: {
    insertRecitals: '#freeFormRecitalText',
    orderText: '#freeFormOrderedText',
    courtsOrder: {
      id: '#orderOnCourtsList',
      options: {
        courtOwnInitiativeOrder: 'Order on court\'s own initiative',
        withoutNoticeOrder: 'Order without notice',
        noneOrder: 'None',
      },
      onInitiativeSelectionDateId: 'onInitiativeSelectionDate',
      withoutNoticeSelectionDateId: 'withoutNoticeSelectionDate',
      courtInitiativeOrderText: 'textarea[id*="onInitiativeSelectionTextArea"]',
      courWithoutNoticeOrderText: 'textarea[id*="withoutNoticeSelectionTextArea"]',
    },
  },

  async fillFreeFormOrder(orderType, formType, workingDay) {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFreeFormOrder', 5);
    await I.see('Test Inc v Sir John Doe');
    await I.see('Recitals and order');
    await I.fillField(this.fields.insertRecitals, 'Test Recitals');
    await I.see('Order');
    await I.fillField(this.fields.orderText, 'Test Order');

    switch (formType) {
      case 'courtOwnInitiativeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtsOrder.courtInitiativeOrderText)).trim(), formType);
        await date.verifyPrePopulatedDate(this.fields.courtsOrder.onInitiativeSelectionDateId, orderType, workingDay);
        break;
      case 'withoutNoticeOrder':
        await selectCourtsOrderType((await I.grabValueFrom(this.fields.courtsOrder.courWithoutNoticeOrderText)).trim(), formType);
        await date.verifyPrePopulatedDate(this.fields.courtsOrder.withoutNoticeSelectionDateId, orderType, workingDay);
        break;
      case 'noneOrder':
        await selectCourtsOrderType('', formType, '');
        break;
    }
    await I.clickContinue();
  },

  async verifyFreeFromErrorMessage() {
    await I.waitInUrl('/GENERATE_DIRECTIONS_ORDER/GENERATE_DIRECTIONS_ORDERFreeFormOrder', 5);
    await I.click('Continue');
    await I.seeNumberOfVisibleElements('.error-message', 3);
  }
};

