const { I } = inject();
const postcodeLookup = require('./addressPostCodeLookupLRspec');

module.exports = {
  fields: function (party) {
    return {
      idYes: `#spec${party}CorrespondenceAddressRequired_Yes`,
      idNo: `#spec${party}CorrespondenceAddressRequired_No`,
      container: `#spec${party}CorrespondenceAddressdetails_spec${party}CorrespondenceAddressdetails`,
    };
  },

  async enterSpecParty(party, address) {
    if (address) {
      I.waitForElement(this.fields(party).idYes);
      await I.runAccessibilityTest();
      await I.click(this.fields(party).idYes);
      await within(this.fields(party).container, () => {
        postcodeLookup.enterAddressManually(party, address);
      });
    } else {
      I.waitForElement(this.fields(party).idNo);
      await I.runAccessibilityTest();
      await I.click(this.fields(party).idNo);
    }

    await I.clickContinue();
  },
};
