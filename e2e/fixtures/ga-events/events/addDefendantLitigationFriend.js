const { buildAddress } = require('../../../api/dataHelper');

module.exports = {
  valid: {
    DefendantLitigationFriend: {
      respondent11LitigationFriend: {
        fullName: 'Bob the litigant friend',
        hasSameAddressAsLitigant: 'No',
        primaryAddress: buildAddress('litigant friend')
      }
    }
  }
};
