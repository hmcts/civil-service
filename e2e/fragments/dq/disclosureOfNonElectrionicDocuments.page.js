const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      directionsForDisclosureProposed: {
        id: `#${party}DQDisclosureOfNonElectronicDocuments_directionsForDisclosureProposed`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      standardDirectionsRequired: {
        id: `#${party}DQDisclosureOfNonElectronicDocuments_standardDirectionsRequired`,
        options: {
          yes: `#${party}DQDisclosureOfNonElectronicDocuments_standardDirectionsRequired_Yes`,
          no: `#${party}DQDisclosureOfNonElectronicDocuments_standardDirectionsRequired_No`
        }
      },
      bespokeDirections: `#${party}DQDisclosureOfNonElectronicDocuments_bespokeDirections`
    };
  },

  async enterDirectionsProposedForDisclosure(party) {
    I.waitForElement(this.fields(party).directionsForDisclosureProposed.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).directionsForDisclosureProposed.id, () => {
      I.click(this.fields(party).directionsForDisclosureProposed.options.yes);
    });
    await within(this.fields(party).standardDirectionsRequired.id, () => {
      I.click(this.fields(party).standardDirectionsRequired.options.no);
    });
    I.fillField(this.fields(party).bespokeDirections, 'Bespoke directions');

    await I.clickContinue();
  }
};
