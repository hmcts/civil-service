const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    claimantType: {
      id: '#claimant_type',
      options: {
        individual: 'Individual',
        company: 'Company',
        organisation: 'Organisation',
        soleTrader: 'Sole trader',
      }
    },
    company: {
      name: '#claimant_companyName'
    },
    address: '#claimant_applicantAddress_applicantAddress',
  },

  async enterClaimant(address) {
    await within(this.fields.claimantType.id, () => {
      I.click(this.fields.claimantType.options.company);
    });

    I.fillField(this.fields.company.name, 'Example company');

    await within(this.fields.address, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.clickContinue();
  }
};

