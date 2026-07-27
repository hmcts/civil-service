const {I} = inject();
const config = require('./../../config');

module.exports = {
  fields: function(mpScenario) {
    switch (mpScenario) {
      case 'ClaimantResponse': {
        return {
          oldFields: {
            chooseCourtLocation: {
              id: '#applicant1DQRequestedCourt_responseCourtCode'
            },
            reasonForHearingAtSpecificCourt: '#applicant1DQRequestedCourt_reasonForHearingAtSpecificCourt'
          },
          fields: {
            responseCourtLocations: {
              id: 'select[id$="applicant1DQRequestedCourt_responseCourtLocations"]',
              options: {
                preferredCourt: config.claimantSelectedCourt
              }
            },
            reasonForHearingAtSpecificCourt: '#applicant1DQRequestedCourt_reasonForHearingAtSpecificCourt',
            remoteHearingRequested: {
              id: '#applicant1DQRemoteHearingLRspec_remoteHearingRequested_radio',
              options: {
                yes: '#applicant1DQRemoteHearingLRspec_remoteHearingRequested_Yes',
                no: '#applicant1DQRemoteHearingLRspec_remoteHearingRequested_No'
              }
            },
            reasonForRemoteHearing: '#applicant1DQRemoteHearingLRspec_reasonForRemoteHearing'
          }
        };
      }

     case 'DefendantResponse2': {
        return {
          oldFields: {
            chooseCourtLocation: {
              id: '#responseClaimCourtLocation2Required_radio',
              options: {
                yes: '#responseClaimCourtLocation2Required_rYes',
                no: '#responseClaimCourtLocation2Required_rNo'
              }
            },
          },
          fields: {
            responseCourtLocations: {
              id: 'select[id$="respondToCourtLocation2_responseCourtLocations"]',
              options: {
                preferredCourt: config.defendant2SelectedCourt
              }
            },
            reasonForHearingAtSpecificCourt: 'textarea[id$="respondToCourtLocation2_reasonForHearingAtSpecificCourt"]',
            remoteHearingRequested: {
              id: '#respondent2DQRemoteHearingLRspec_remoteHearingRequested_radio',
              options: {
                yes: '#respondent2DQRemoteHearingLRspec_remoteHearingRequested_Yes',
                no: '#respondent2DQRemoteHearingLRspec_remoteHearingRequested_No'
              }
            },
            reasonForRemoteHearing: '#respondent2DQRemoteHearingLRspec_reasonForRemoteHearing'
          }
        };
       }

      case 'DefendantResponse':
      default: {
        return {
          oldFields: {
            chooseCourtLocation: {
              id: '#responseClaimCourtLocationRequired_radio',
              options: {
                yes: '#responseClaimCourtLocationRequired_Yes',
                no: '#responseClaimCourtLocationRequired_No'
              }
            }
          },
          fields: {
            responseCourtLocations: {
              id: 'select[id$="responseCourtLocations"]',
              options: {
                preferredCourt: config.defendantSelectedCourt
              }
            },
            reasonForHearingAtSpecificCourt: 'textarea[id$="reasonForHearingAtSpecificCourt"]',
            remoteHearingRequested: {
              id: '#respondent1DQRemoteHearingLRspec_remoteHearingRequested_radio',
              options: {
                yes: '#respondent1DQRemoteHearingLRspec_remoteHearingRequested_Yes',
                no: '#respondent1DQRemoteHearingLRspec_remoteHearingRequested_No'
              }
            },
            reasonForRemoteHearing: '#respondent1DQRemoteHearingLRspec_reasonForRemoteHearing'
          }
        };
      }
    }
  },

  async chooseCourt(mpScenario) {
    I.waitForElement(this.fields(mpScenario).fields.responseCourtLocations.id);
    await I.runAccessibilityTest();
    I.selectOption(this.fields(mpScenario).fields.responseCourtLocations.id,
    this.fields(mpScenario).fields.responseCourtLocations.options.preferredCourt);
    I.fillField(this.fields(mpScenario).fields.reasonForHearingAtSpecificCourt, 'Some reason');
    await within(this.fields(mpScenario).fields.remoteHearingRequested.id, () => {
      I.click(this.fields(mpScenario).fields.remoteHearingRequested.options.yes);
    });
    I.fillField(this.fields(mpScenario).fields.reasonForRemoteHearing, 'Some reason');
    await I.clickContinue();
  }
};