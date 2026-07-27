const {I} = inject();
const litigationFriend = require('../../fragments/litigationFriend');

module.exports = {
  async enterLitigantFriendWithDifferentAddressToDefendant(partyType = 'respondent1', address, file) {
    await I.runAccessibilityTest();
    await I.see('You should only be adding a litigation friend for an individual');
    await litigationFriend.enterLitigantFriendWithDifferentAddressToLitigant(partyType === 'both' ? 'generic' : partyType, address, file);
    await I.clickContinue();
  }
};

