const {I} = inject();
const postcodeLookup = require('./addressPostcodeLookup');

module.exports = {
  fields: function (partyType) {
    return {
      type: {
        id: `#${partyType}_type`,
        options: {
          individual: 'Individual',
          company: 'Company',
          organisation: 'Organisation',
          soleTrader: 'Sole trader',
        }
      },
      company: {
        name: `#${partyType}_companyName`
      },
      address: `#${partyType}_primaryAddress_primaryAddress`,
    };
  },

  async enterParty(partyType, address) {
    await within(this.fields(partyType).type.id, () => {
      I.click(this.fields(partyType).type.options.company);
    });

    I.fillField(this.fields(partyType).company.name, 'Example company');

    await within(this.fields(partyType).address, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.clickContinue();
  }
}
;

