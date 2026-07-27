const {I} = inject();

module.exports = {
  fields: {
    recitals: {
      id: '#freeFormRecordedTextArea',
    },
    order: {
      id: '#freeFormOrderedTextArea',
    },
    orderOnCourtsList: {
      id: '#orderOnCourtsList',
      options: {
        courtsInitiative: '#orderOnCourtsList-ORDER_ON_COURT_INITIATIVE',
        withoutNotice: '#orderOnCourtsList-ORDER_WITHOUT_NOTICE',
        notApplicable: '#orderOnCourtsList-NOT_APPLICABLE',
      }
    },
    hearingNotes: {
      id: '#freeFormHearingNotes'
    }
  },

  async enterOrderDetails() {
    await I.waitForText('Recitals');
    await I.runAccessibilityTest();
    await I.fillField(this.fields.recitals.id, 'The court records that...');
    await I.fillField(this.fields.order.id, 'The court orders that...');
    await I.click(this.fields.recitals.id);
    await I.click(this.fields.orderOnCourtsList.options.courtsInitiative);
    await I.fillField(this.fields.hearingNotes.id, 'Hearing notes...');
    await I.clickContinue();
  },
};