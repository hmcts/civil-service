const postcodeLookup = require('../../fragments/addressPostcodeLookup');
const {I} = inject();

module.exports = {

  fields: {
    respondent1LitigationFriend: {
      id: '#respondent1LitigationFriend_respondent1LitigationFriend',
      firstName: '#respondent1LitigationFriend_firstName',
      diffAddress: '#respondent1LitigationFriend_hasSameAddressAsLitigant_No',
      addressId:'#respondent1LitigationFriend_primaryAddress_primaryAddress'
    },
  },

  async updateLitigationFriend(address) {
    I.waitForElement(this.fields.respondent1LitigationFriend.id);
    await I.runAccessibilityTest();

    I.click(this.fields.respondent1LitigationFriend.diffAddress);

    I.waitForElement(this.fields.respondent1LitigationFriend.addressId);
    await within(this.fields.respondent1LitigationFriend.addressId, () => {
      postcodeLookup.enterAddressManually(address, locate('a').withText('I can\'t enter a UK postcode')
        .inside(locate('div#respondent1LitigationFriend_primaryAddress_primaryAddress')));
    });

    await I.clickContinue();
  },
};
