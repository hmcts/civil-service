const {I} = inject();
const litigationFriend = require('../../fragments/litigationFriend');

module.exports = {

  fields: {
    childApplicant: {
      id: '#applicant1LitigationFriendRequired',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterLitigantFriendWithDifferentAddressToApplicant(address, file) {
    I.waitForElement(this.fields.childApplicant.id);
    await within(this.fields.childApplicant.id, () => {
      I.click(this.fields.childApplicant.options.yes);
    });

   await litigationFriend.enterLitigantFriendWithDifferentAddressToLitigant('applicant1', address, file);

    await I.clickContinue();
  }
};

