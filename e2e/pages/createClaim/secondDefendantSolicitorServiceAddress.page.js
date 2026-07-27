const { I } = inject();

const address = require('./../../fixtures/address.js');
const postcodeLookup = require('./../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    respondentSolicitor2ServiceAddressRequired: {
      id: '#respondentSolicitor2ServiceAddressRequired',
      options: {
        yes: '#respondentSolicitor2ServiceAddressRequired_Yes',
        no: '#respondentSolicitor2ServiceAddressRequired_No'
      }
    },
    respondentSolicitor2ServiceAddress: '#respondentSolicitor2ServiceAddress_respondentSolicitor2ServiceAddress'
  },

  async enterOrganisationServiceAddress() {
    I.waitForElement(this.fields.respondentSolicitor2ServiceAddressRequired.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondentSolicitor2ServiceAddressRequired.id, () => {
      I.click(this.fields.respondentSolicitor2ServiceAddressRequired.options.yes);
    });

    await within(this.fields.respondentSolicitor2ServiceAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });
    await I.clickContinue();
  }
};

