const {I} = inject();
const config = require('./../../config');

module.exports = {
  oldFields: function (party) {
    return {
      responseCourtCode: `#${party}DQRequestedCourt_responseCourtCode`,
    };
  },
  fields: function (party) {
    return {
      requestHearingAtSpecificCourt: {
        id: `#${party}DQRequestedCourt_requestHearingAtSpecificCourt`,
        options: {
          yes: `#${party}DQRequestedCourt_requestHearingAtSpecificCourt_Yes`,
          no: `#${party}DQRequestedCourt_requestHearingAtSpecificCourt_No`
        }
      },
      remoteHearingRequested: {
        id: `#${party}DQRemoteHearing_remoteHearingRequested`,
        options: {
          yes: `#${party}DQRemoteHearing_remoteHearingRequested_Yes`,
          no: `#${party}DQRemoteHearing_remoteHearingRequested_No`
        }
      },
      remoteHearingSpecRequested: {
        id: `#${party}DQRemoteHearingLRspec_remoteHearingRequested`,
        options: {
          yes: `#${party}DQRemoteHearingLRspec_remoteHearingRequested_Yes`,
          no: `#${party}DQRemoteHearingLRspec_remoteHearingRequested_No`
        }
      },
      reasonForRemoteHearing: `#${party}DQRemoteHearing_reasonForRemoteHearing`,
      reasonForHearingAtSpecificCourt: `#${party}DQRequestedCourt_reasonForHearingAtSpecificCourt`,
      courtLocation: {
        id: `#${party}DQRequestedCourt_responseCourtLocations`,
        options: {
          defendantPreferredCourt: config.defendantSelectedCourt
        }
      }
    };
  },

  async selectSpecificCourtForHearing(party) {
    I.waitForElement(this.fields(party).requestHearingAtSpecificCourt.id);
    await I.runAccessibilityTest();

    I.selectOption(this.fields(party).courtLocation.id, this.fields(party).courtLocation.options.defendantPreferredCourt);

    I.fillField(this.fields(party).reasonForHearingAtSpecificCourt, 'A reason for the court');
    await within(this.fields(party).remoteHearingRequested.id, () => {
      I.click(this.fields(party).remoteHearingRequested.options.yes);
    });

    I.fillField(this.fields(party).reasonForRemoteHearing, 'Reason for remote hearing');
    await I.clickContinue();
  },

  async selectSpecCourtLocation(party) {
    I.waitForElement(this.fields(party).courtLocation.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields(party).courtLocation.id, this.fields(party).courtLocation.options.defendantPreferredCourt);
    await within(this.fields(party).remoteHearingSpecRequested.id, () => {
      I.click(this.fields(party).remoteHearingSpecRequested.options.no);
    });
    await I.clickContinue();
  },

};
