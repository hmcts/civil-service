const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      hearingLength: {
        id: `#${party}DQHearingFastClaim_hearingLength`,
        options: {
          lessThanOneDay: 'Less than a day',
          oneDay: 'One day',
          moreThanOneDay: 'More than a day',
        }
      },
      hearingLengthHours: `#${party}DQHearingFastClaim_hearingLengthHours`,
      hearingLengthDays: `#${party}DQHearingFastClaim_hearingLengthDays`,
      unavailableDatesRequired: {
        id: `#${party}DQHearingFastClaim_unavailableDatesRequired`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      unavailableDates: {
        id: `#${party}DQHearingFastClaim_unavailableDatesLRspec`,
        element: {
          who: `#${party}DQHearingFastClaim_unavailableDatesLRspec_0_who`,
          date: 'date',
        }
      },
    };
  },

  async enterHearing(party) {
    I.waitForElement(this.fields(party).hearingLength.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).hearingLength.id, () => {
      I.click(this.fields(party).hearingLength.options.lessThanOneDay);
    });

    I.fillField(this.fields(party).hearingLengthHours, '5');
    await within(this.fields(party).unavailableDatesRequired.id, () => {
      I.click(this.fields(party).unavailableDatesRequired.options.no);
    });

    await I.clickContinue();
  },
};
