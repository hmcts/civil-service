const {I} = inject();
const litigationFriend = require('../../fragments/litigationFriend');

module.exports = {

  fields: (party) => {
    return {
      childApplicant: {
        id: `#${party}LitigationFriendRequired`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
    };
  },

  async enterLitigantFriend(party = 'applicant1', address, file) {
    I.waitForElement(this.fields(party).childApplicant.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).childApplicant.id, () => {
      const { yes, no } = this.fields(party).childApplicant.options;
      I.click(address ? yes : no);
    });

    if(address) {
      await litigationFriend.enterLitigantFriendWithDifferentAddressToLitigant(party, address, file);
    }

    await I.clickContinue();
  }
};

