const {I} = inject();
const postcodeLookup = require('./addressPostcodeLookup');

module.exports = {

  fields: function (partyType) {
    return {
      litigationFriendName: `#${partyType}LitigationFriend_fullName`,
      litigantInFriendDifferentAddress: {
        id: `#${partyType}LitigationFriend_hasSameAddressAsLitigant`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      litigantInFriendAddress: `#${partyType}LitigationFriend_primaryAddress_primaryAddress`,
      certificateOfSuitability: `#${partyType}LitigationFriend_certificateOfSuitability_value`
    };
  },

  async enterLitigantFriendWithDifferentAddressToLitigant(partyType, address, file) {
    I.fillField(this.fields(partyType).litigationFriendName, 'John Smith');

    await within(this.fields(partyType).litigantInFriendDifferentAddress.id, () => {
      I.click(this.fields(partyType).litigantInFriendDifferentAddress.options.no);
    });

    await within(this.fields(partyType).litigantInFriendAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields(partyType).certificateOfSuitability);
    I.attachFile(this.fields(partyType).certificateOfSuitability, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
  }
};

