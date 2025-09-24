const {I} = inject();

module.exports = {

  fields: {
    addFlightDelayClaim: {
      id: '#isFlightDelayClaim',
      options: {
        yes: '#isFlightDelayClaim_Yes',
        no: '#isFlightDelayClaim_No'
      }
    },
  },

  async enteredFlightDelayClaim(addAnotherDefendant) {
    I.waitForElement(this.fields.addFlightDelayClaim.id);
    await I.runAccessibilityTest();
    await within(this.fields.addFlightDelayClaim.id, () => {
      const { yes, no } = this.fields.addFlightDelayClaim.options;
      I.click(addAnotherDefendant ? yes : no);
    });

    await I.clickContinue();
  },

  async enteredFlightDelayClaimYes(addAnotherDefendant) {
    I.waitForElement(this.fields.addFlightDelayClaim.id);
    await I.runAccessibilityTest();
    await within(this.fields.addFlightDelayClaim.id, () => {
      const { no, yes } = this.fields.addFlightDelayClaim.options;
      I.click(addAnotherDefendant ? no : yes);
    });

    I.see('Is this an airline claim?');
    I.see('Enter flight details');
    I.see('Airline');
    I.see('Flight number');
    I.see('Date of flight');
    I.see('For example, 16 04 2021');
    I.click('Continue');
    I.see('Airline is required');
    I.see('Flight number is required');
    I.see('Date of flight is required');
    I.selectOption('#flightDelayDetails_airlineList', 'KLM');
    I.fillField('#flightDelayDetails_flightNumber', 10001);
    I.click('Continue');
    I.see('Date of flight is required');
    I.fillField('#scheduledDate-day', 1);
    I.click('Continue');
    I.see('The data entered is not valid for Date of flight');
    I.fillField('#scheduledDate-month', 1);
    I.fillField('#scheduledDate-year', 2035);
    I.click('Continue');
    I.waitForText('Scheduled date of flight must be today or in the past','5');
    I.fillField('#scheduledDate-year', 2023);
    await I.clickContinue();
  }

};

