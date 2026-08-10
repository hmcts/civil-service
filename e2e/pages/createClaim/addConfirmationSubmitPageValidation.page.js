const {I} = inject();

module.exports = {

  async flightDelayClaimConfirmationPageValidation() {
    I.waitForElement('#isFlightDelayClaimLbl');
    I.see('Is this an airline claim?');
    I.see('Enter flight details');
    I.see('Airline');
    I.see('Flight number');
    I.see('Date of flight');
    I.see('KLM');
  }
};

