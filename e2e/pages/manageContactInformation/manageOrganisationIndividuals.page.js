const {I} = inject();

module.exports = {

  fields: {
    updateOrgIndividualsForm: {
      id: '#updateOrgIndividualsForm',
      element: {
        firstName: '#updateOrgIndividualsForm_0_firstName',
        lastName: '#updateOrgIndividualsForm_0_lastName',
        emailAddress: '#updateOrgIndividualsForm_0_emailAddress',
        phoneNumber: '#updateOrgIndividualsForm_0_phoneNumber'
      }
    },
  },

  async addOrgIndividuals() {
    await I.waitForElement(this.fields.updateOrgIndividualsForm.id, 60);
    await I.runAccessibilityTest();

    await I.addAnotherElementToCollection('//div[@id=\'updateOrgIndividualsForm\']//button[@type=\'button\' and contains(text(), \'Add new\')]');

    I.waitForElement(this.fields.updateOrgIndividualsForm.element.firstName);
    I.fillField(this.fields.updateOrgIndividualsForm.element.firstName, 'Kendal');
    I.fillField(this.fields.updateOrgIndividualsForm.element.lastName, 'Bloom');
    I.fillField(this.fields.updateOrgIndividualsForm.element.emailAddress, 'kendalbloom@email.com');
    I.fillField(this.fields.updateOrgIndividualsForm.element.phoneNumber, '07821016453');
    await I.clickContinue();
  },
};
