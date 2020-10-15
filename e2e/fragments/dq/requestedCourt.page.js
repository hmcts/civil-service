const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      requestHearingAtSpecificCourt: {
        id: `#${party}DQRequestedCourt_requestHearingAtSpecificCourt`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      name: `#${party}DQRequestedCourt_name`,
      reasonForHearingAtSpecificCourt: `#${party}DQRequestedCourt_reasonForHearingAtSpecificCourt`,
    };
  },

  async selectSpecificCourtForHearing(party) {
    I.waitForElement(this.fields(party).requestHearingAtSpecificCourt.id);
    await within(this.fields(party).requestHearingAtSpecificCourt.id, () => {
      I.click(this.fields(party).requestHearingAtSpecificCourt.options.yes);
    });

    I.fillField(this.fields(party).name, 'A court name');
    I.fillField(this.fields(party).reasonForHearingAtSpecificCourt, 'A reason for the court');
    await I.clickContinue();
  },
};
