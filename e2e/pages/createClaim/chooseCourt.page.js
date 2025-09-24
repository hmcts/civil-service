const {I} = inject();
const config = require('./../../config');

module.exports = {
  fields: {
    courtLocation: {
      id: '#courtLocation_applicantPreferredCourtLocationList',
      options: {
        claimantPreferredCourt: config.claimantSelectedCourt
      }
    },
    reasonForHearingAtSpecificCourt: '#courtLocation_reasonForHearingAtSpecificCourt',
    remoteHearingRequested: {
      id: '#applicant1DQRemoteHearing_remoteHearingRequested_radio',
      options: {
        yes: '#applicant1DQRemoteHearing_remoteHearingRequested_Yes',
        no: '#applicant1DQRemoteHearing_remoteHearingRequested_No'
      }
    },
    reasonForRemoteHearing: '#applicant1DQRemoteHearing_reasonForRemoteHearing'
  },


  async selectCourt() {
    I.waitForElement(this.fields.courtLocation.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields.courtLocation.id, this.fields.courtLocation.options.claimantPreferredCourt);
    await within(this.fields.remoteHearingRequested.id, () => {
      I.click(this.fields.remoteHearingRequested.options.yes);
    });
    I.fillField(this.fields.reasonForRemoteHearing, 'Some reason');
    await I.clickContinue();
  }
};
