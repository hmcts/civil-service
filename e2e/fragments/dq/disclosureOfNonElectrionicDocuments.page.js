const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      directionsProposedForDisclosure: `#${party}DQDisclosureOfNonElectronicDocuments`,
    };
  },

  async enterDirectionsProposedForDisclosure(party) {
    I.waitForElement(this.fields(party).directionsProposedForDisclosure);
    I.fillField(this.fields(party).directionsProposedForDisclosure, 'Reason for no agreement');

    await I.clickContinue();
  }
};
