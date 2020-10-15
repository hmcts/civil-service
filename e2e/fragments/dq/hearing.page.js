const {I} = inject();
const date = require('../../fragments/date');

module.exports = {

  fields: function (party) {
    return {
      hearingLength: {
        id: `#${party}DQHearing_hearingLength`,
        options: {
          lessThanOneDay: 'Less than a day',
          oneDay: 'One day',
          moreThanOneDay: 'More than a day',
        }
      },
      hearingLengthHours: `#${party}DQHearing_hearingLengthHours`,
      hearingLengthDays: `#${party}DQHearing_hearingLengthDays`,
      unavailableDatesRequired: {
        id: `#${party}DQHearing_unavailableDatesRequired`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      unavailableDates: {
        id: `#${party}DQHearing_unavailableDates`,
        element: {
          who: `#${party}DQHearing_unavailableDates_0_who`,
          date: `${party}DQHearing_unavailableDates_0_date`,
        }
      },
    };
  },

  async enterHearingInformation(party) {
    I.waitForElement(this.fields(party).hearingLength.id);
    await within(this.fields(party).hearingLength.id, () => {
      I.click(this.fields(party).hearingLength.options.lessThanOneDay);
    });

    I.fillField(this.fields(party).hearingLengthHours, '5');
    await within(this.fields(party).unavailableDatesRequired.id, () => {
      I.click(this.fields(party).unavailableDatesRequired.options.yes);
    });

    await this.addUnavailableDates(party);
    await I.clickContinue();
  },

  async addUnavailableDates(party) {
    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields(party).unavailableDates.element.who);
    I.fillField(this.fields(party).unavailableDates.element.who, 'John Smith');
    await date.enterDate(this.fields(party).unavailableDates.element.date);
  },
};
