const { I } = inject();
const postcodeLookup = require('./addressPostCodeLookupLRspec');

module.exports = {
  fields: function (party) {
    return {
      id: `#spec${party}CorrespondenceAddressRequired_Yes`,
      container: `#spec${party}CorrespondenceAddressdetails_spec${party}CorrespondenceAddressdetails`,
    };
  },

  async enterSpecParty(party, address) {
    I.waitForElement(this.fields(party).id);
    await I.runAccessibilityTest();
    await I.click('Yes');
    await within(this.fields(party).container, () => {
      postcodeLookup.enterAddressManually(party, address);
    });

    await I.clickContinue();
  },
};
