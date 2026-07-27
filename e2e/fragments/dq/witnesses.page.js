const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      witnessesToAppear: {
        id: `#${party}DQWitnesses_witnessesToAppear`,
        options: {
          yes: `#${party}DQWitnesses_witnessesToAppear_Yes`,
          no: `#${party}DQWitnesses_witnessesToAppear_No`
        }
      },
      witnessDetails: {
        id: `#${party}DQWitnesses_details`,
        element: {
          firstName: `#${party}DQWitnesses_details_0_firstName`,
          lastName: `#${party}DQWitnesses_details_0_lastName`,
          emailAddress: `#${party}DQWitnesses_details_0_emailAddress`,
          phoneNumber: `#${party}DQWitnesses_details_0_phoneNumber`,
          reasonForWitness: `#${party}DQWitnesses_details_0_reasonForWitness`,
        }
      },
      witnessDetails_oldFields: {
        id: `#${party}DQWitnesses_details`,
        element: {
          name: `#${party}DQWitnesses_details_0_name`,
          reasonForWitness: `#${party}DQWitnesses_details_0_reasonForWitness`,
        }
      },
    };
  },

  async enterWitnessInformation(party) {
    I.waitForElement(this.fields(party).witnessesToAppear.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).witnessesToAppear.id, () => {
      I.click(this.fields(party).witnessesToAppear.options.yes);
    });
    await this.addWitness(party);
    await I.clickContinue();
  },
  async addWitness(party) {
    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields(party).witnessDetails.element.firstName);
    I.fillField(this.fields(party).witnessDetails.element.firstName, 'John');
    I.fillField(this.fields(party).witnessDetails.element.lastName, 'Smith');
    I.fillField(this.fields(party).witnessDetails.element.emailAddress, 'johnsmith@email.com');
    I.fillField(this.fields(party).witnessDetails.element.phoneNumber, '07821016453');
    I.fillField(this.fields(party).witnessDetails.element.reasonForWitness, 'Reason for witness');
  },
};
