const {I} = inject();

const date = require('../../fragments/date');

module.exports = {

  fields: {
    transferredDate: {
      id: 'date'
    },
    claimProceedsInCasemanReason: {
      id: '#claimProceedsInCaseman_reason',
      options: {
        other: 'Other'
      }
    },
    otherDescription: {
      id: '#claimProceedsInCaseman_other'
    },
  },

  async enterTransferDate() {
    await I.runAccessibilityTest();
    await date.enterDate(this.fields.transferredDate.id, -1);
    await within(this.fields.claimProceedsInCasemanReason.id, () => {
      I.click(this.fields.claimProceedsInCasemanReason.options.other);
    });

    I.fillField(this.fields.otherDescription.id, 'A reason other than Application and Judgement request.');

    await I.clickContinue();
  }
};
