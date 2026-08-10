const {I} = inject();
const config = require('./../../config');

module.exports = {
  fields: {
    courtLocation: {
      id: 'select[id$="transferCourtLocationList"]',
      options: {
        claimantPreferredCourt: config.claimantSelectedCourt,
        claimantPreferredCourt2: config.liverpoolCourt
      }
    },
    reasonForHearingAtSpecificCourt: '#reasonForTransfer',
  },


  async selectCourt() {
    I.waitForElement(this.fields.courtLocation.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.courtLocation.id, this.fields.courtLocation.options.claimantPreferredCourt2);
    I.fillField(this.fields.reasonForHearingAtSpecificCourt, 'Some reason');
    await I.clickContinue();
  }
};
