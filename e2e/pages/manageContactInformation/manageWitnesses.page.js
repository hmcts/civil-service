const {I} = inject();

module.exports = {

  fields: {
    witnesses: {
      id: '#updateWitnessesDetailsForm',
      element: {
        firstName: '#updateWitnessesDetailsForm_1_firstName',
        lastName: '#updateWitnessesDetailsForm_1_lastName',
        emailAddress: '#updateWitnessesDetailsForm_1_emailAddress',
        phoneNumber: '#updateWitnessesDetailsForm_1_phoneNumber'
      }
    },
  },

  async addWitness() {
    I.waitForElement(this.fields.witnesses.id);
    await I.runAccessibilityTest();

    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields.witnesses.element.firstName);
    I.fillField(this.fields.witnesses.element.firstName, 'Leia');
    I.fillField(this.fields.witnesses.element.lastName, 'Johnson');
    I.fillField(this.fields.witnesses.element.emailAddress, 'leiajohnson@email.com');
    I.fillField(this.fields.witnesses.element.phoneNumber, '07821016453');
    await I.clickContinue();
  },
};
