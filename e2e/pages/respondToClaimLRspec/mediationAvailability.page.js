const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      yes: `#${party}MediationAvailability_isMediationUnavailablityExists_Yes`,
      no: `#${party}MediationAvailability_isMediationUnavailablityExists_No`,
    };
  },

  async selectNoMediationAvailability(party) {
    await I.click(this.fields(party).no);
    await I.clickContinue();
  }
};
