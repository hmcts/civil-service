const {I} = inject();
const litigationFriend = require('../../fragments/litigationFriend');

module.exports = {

  fields: (party) => {
    return {
      childApplicant: {
        id: `#${party}LitigationFriendRequired`,
        options: {
          yes: `#${party}LitigationFriendRequired_Yes`,
          no: `#${party}LitigationFriendRequired_No`
        }
      },
    };
  },

  async enterLitigantFriend(party = 'applicant1', address, file) {
    I.waitForElement(this.fields(party).childApplicant.id);
    await I.runAccessibilityTest();
    const { yes, no } = this.fields(party).childApplicant.options;
    I.click(address ? yes : no);

    if(address) {
      await litigationFriend.enterLitigantFriendWithDifferentAddressToLitigant(party, address, file);
    }

    await I.wait(10);
    await I.clickContinue();
  }
};

