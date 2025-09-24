const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    transferredDate: {
      id: 'date'
    },
    claimProceedsInCasemanReason: {
      id: '#claimProceedsInCasemanLR_reason-OTHER',
      options: {
        other: 'Other'
      }
    },
    otherDescription: {
      id: '#claimProceedsInCasemanLR_other'
    },
  },

  async enterTransferDate() {
    await I.runAccessibilityTest();
    await date.enterDate(this.fields.transferredDate.id, -1);
    I.click(this.fields.claimProceedsInCasemanReason.id);
    I.fillField(this.fields.otherDescription.id, 'A reason other than Application and Judgement request.');
    await I.click('Submit');
  }
};
