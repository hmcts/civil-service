const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    childClaimant: {
      id: '#applicant1LitigationFriend_required',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    litigationFriendName: '#applicant1LitigationFriend_fullName',
    litigantInFriendDifferentAddress: {
      id: '#applicant1LitigationFriend_hasSameAddressAsLitigant',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    litigantInFriendAddress: '#applicant1LitigationFriend_primaryAddress_primaryAddress',
    certificateOfSuitability: '#applicant1LitigationFriend_certificateOfSuitability_0'
  },

  async enterLitigantFriendWithDifferentAddressToClaimant(address, file) {
    I.waitForElement(this.fields.childClaimant.id);
    await within(this.fields.childClaimant.id, () => {
      I.click(this.fields.childClaimant.options.yes);
    });

    I.fillField(this.fields.litigationFriendName, 'John Smith');

    await within(this.fields.litigantInFriendDifferentAddress.id, () => {
      I.click(this.fields.litigantInFriendDifferentAddress.options.no);
    });

    await within(this.fields.litigantInFriendAddress, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields.certificateOfSuitability);
    I.attachFile(this.fields.certificateOfSuitability, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await I.clickContinue();
  }
};

