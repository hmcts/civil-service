const { I } = inject();

const address = require('./../../fixtures/address.js');
const postcodeLookup = require('./../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    respondentSolicitor1ServiceAddressRequired: {
      id: '#respondentSolicitor1ServiceAddressRequired',
      options: {
        yes: '#respondentSolicitor1ServiceAddressRequired_Yes',
        no: '#respondentSolicitor1ServiceAddressRequired_No'
      }
    },
    respondentSolicitor1ServiceAddress: '#respondentSolicitor1ServiceAddress_respondentSolicitor1ServiceAddress'
  },

  async enterOrganisationServiceAddress() {
    I.waitForElement(this.fields.respondentSolicitor1ServiceAddressRequired.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondentSolicitor1ServiceAddressRequired.id, () => {
      I.click(this.fields.respondentSolicitor1ServiceAddressRequired.options.yes);
    });

    await within(this.fields.respondentSolicitor1ServiceAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.clickContinue();
  }
};

