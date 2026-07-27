const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      hearingLength: {
        id: `#${party}DQHearingLRspec_hearingLength`,
        options: {
          lessThanOneDay: 'Less than a day',
          oneDay: 'One day',
          moreThanOneDay: 'More than a day',
        }
      },
      hearingLengthHours: `#${party}DQHearingLRspec_hearingLengthHours`,
      hearingLengthDays: `#${party}DQHearingLRspec_hearingLengthDays`,
      unavailableDatesRequired: {
        id: `#${party}DQHearingLRspec_unavailableDatesRequired`,
        options: {
          yes: `#${party}DQHearingLRspec_unavailableDatesRequired_Yes`,
          no: `#${party}DQHearingLRspec_unavailableDatesRequired_No`
        }
      },
      unavailableDates: {
        id: `#${party}DQHearingLRspec_unavailableDatesLRspec`,
        element: {
          who: `#${party}DQHearingLRspec_unavailableDatesLRspec_0_who`,
          date: 'date',
        }
      },
    };
  },

  async enterHearing(party) {
    I.waitForElement(this.fields(party).unavailableDatesRequired.id);
    await within(this.fields(party).unavailableDatesRequired.id, () => {
      I.click(this.fields(party).unavailableDatesRequired.options.no);
    });

    await I.clickContinue();
  },
};
