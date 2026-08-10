const address = require('./../../fixtures/address.js');
const postcodeLookup = require('./../../fragments/addressPostcodeLookup');

const {I} = inject();

module.exports = {

  fields: {
    applicantSolicitor1ServiceAddressRequired: {
      id: '#applicantSolicitor1ServiceAddressRequired',
      options: {
        yes: '#applicantSolicitor1ServiceAddressRequired_Yes',
        no: '#applicantSolicitor1ServiceAddressRequired_No'
      }
    },
    applicantSolicitor1ServiceAddress: '#applicantSolicitor1ServiceAddress_applicantSolicitor1ServiceAddress'
  },

  async enterOrganisationServiceAddress() {
    I.waitForElement(this.fields.applicantSolicitor1ServiceAddressRequired.id);
    await I.runAccessibilityTest();
    await within(this.fields.applicantSolicitor1ServiceAddressRequired.id, () => {
      I.click(this.fields.applicantSolicitor1ServiceAddressRequired.options.yes);
    });

    await within(this.fields.applicantSolicitor1ServiceAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.clickContinue();
  }
};

