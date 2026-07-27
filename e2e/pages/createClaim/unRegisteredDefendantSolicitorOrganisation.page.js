const postcodeLookup = require('./../../fragments/addressPostcodeLookup.js');
const address = require('./../../fixtures/address.js');
const { I } = inject();

module.exports = {

  fields: respondentNumber => {
    return {
      respondentSolicitorOrganisationDetails: {
        id: `#respondentSolicitor${respondentNumber}OrganisationDetails_respondentSolicitor${respondentNumber}OrganisationDetails`,
      },
      organisationName: `#respondentSolicitor${respondentNumber}OrganisationDetails_organisationName`,
      phoneNumber: `#respondentSolicitor${respondentNumber}OrganisationDetails_phoneNumber`,
      email: `#respondentSolicitor${respondentNumber}OrganisationDetails_email`,
      dx: `#respondentSolicitor${respondentNumber}OrganisationDetails_dx`,
      fax: `#respondentSolicitor${respondentNumber}OrganisationDetails_fax`,
      solicitorAddress: `#respondentSolicitor${respondentNumber}OrganisationDetails_address_address`
    };
  },

  async enterDefendantSolicitorDetails(respondentNumber = '1') {
    I.waitForElement(this.fields(respondentNumber).respondentSolicitorOrganisationDetails.id);
    I.waitForElement(this.fields(respondentNumber).organisationName);
    I.fillField(this.fields(respondentNumber).organisationName, 'Organisation 3');
    I.waitForElement(this.fields(respondentNumber).phoneNumber);
    I.fillField(this.fields(respondentNumber).phoneNumber, '0123456789');
    I.waitForElement(this.fields(respondentNumber).email);
    I.fillField(this.fields(respondentNumber).email, 'organisation3@hmcts.net');
    I.waitForElement(this.fields(respondentNumber).dx);
    I.fillField(this.fields(respondentNumber).dx, 'dx');
    I.waitForElement(this.fields(respondentNumber).fax);
    I.fillField(this.fields(respondentNumber).fax, '0123456789');

    await within(this.fields(respondentNumber).solicitorAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });
    await I.clickContinue();
  }
};
