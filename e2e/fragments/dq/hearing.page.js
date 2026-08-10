const {I} = inject();
const date = require('../../fragments/date');

module.exports = {

  fields: function (party) {
    return {
      unavailableDatesRequired: {
        id: `#${party}DQHearing_unavailableDatesRequired`,
        options: {
          yes: `#${party}DQHearing_unavailableDatesRequired_Yes`,
          no: `#${party}DQHearing_unavailableDatesRequired_No`
        }
      },
      specHearingAvailableDatesRequired: {
        id: `#${party}DQHearingLRspec_unavailableDatesRequired`,
        options: {
          yes: `#${party}DQHearingLRspec_unavailableDatesRequired_Yes`,
          no: `#${party}DQHearingLRspec_unavailableDatesRequired_No`
        }
      },
      unavailableDates: {
        id: `#${party}DQHearing_unavailableDates`,
        element: {
          unavailableDateType: {
            id: `#${party}DQHearing_unavailableDates_0_unavailableDateType`,
            options: {
              singleDateId: `#${party}DQHearing_unavailableDates_0_unavailableDateType-SINGLE_DATE`,
              dateRangeId: `#${party}DQHearing_unavailableDates_0_unavailableDateType-DATE_RANGE`
            }
          },
          date: 'date',
        }
      },
    };
  },

  async enterHearingInformation(party) {
    I.waitForElement(this.fields(party).unavailableDatesRequired.id);
    await I.runAccessibilityTest();

    await within(this.fields(party).unavailableDatesRequired.id, () => {
      I.click(this.fields(party).unavailableDatesRequired.options.yes);
    });
    await this.addUnavailableDates(party);

    await I.clickContinue();
  },

  async enterHearingAvailability(party) {
    I.waitForElement(this.fields(party).specHearingAvailableDatesRequired.id);
    await I.runAccessibilityTest();

    await within(this.fields(party).specHearingAvailableDatesRequired.id, () => {
      I.click(this.fields(party).specHearingAvailableDatesRequired.options.no);
    });
    await I.clickContinue();
  },

  async addUnavailableDates(party) {
    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields(party).unavailableDates.element.unavailableDateType.id);
    I.forceClick(this.fields(party).unavailableDates.element.unavailableDateType.options.singleDateId);
    await date.enterDate(this.fields(party).unavailableDates.element.date);
  },
};
