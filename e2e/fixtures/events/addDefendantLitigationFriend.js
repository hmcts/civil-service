const address = require('../address');

module.exports = {
  valid: {
    DefendantLitigationFriend: {
      respondent11LitigationFriend: {
        fullName: 'Bob the litigant friend',
        hasSameAddressAsLitigant: 'No',
        primaryAddress: {
          AddressLine1: `${address.buildingAndStreet.lineOne + ' - litigant friend'}`,
          AddressLine2: address.buildingAndStreet.lineTwo,
          AddressLine3: address.buildingAndStreet.lineThree,
          PostTown: address.town,
          County: address.county,
          Country: address.country,
          PostCode: address.postcode
        }
      }
    }
  }
};
