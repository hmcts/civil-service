const {I} = inject();
const litigationFriend = require('../../fragments/litigationFriend');

module.exports = {

  fields: {
    childClaimant: {
      id: '#applicant1LitigationFriendRequired',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterLitigantFriendWithDifferentAddressToClaimant(address, file) {
    I.waitForElement(this.fields.childClaimant.id);
    await within(this.fields.childClaimant.id, () => {
      I.click(this.fields.childClaimant.options.yes);
    });

   await litigationFriend.enterLitigantFriendWithDifferentAddressToLitigant('applicant1', address, file);

    await I.clickContinue();
  }
};

