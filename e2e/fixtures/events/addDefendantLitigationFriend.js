const { buildAddress } = require('../../api/dataHelper');
const uuid = require('uuid');

const docUuid = uuid.v1();

module.exports = {
  ONE_V_ONE: {
    valid: {
      DefendantLitigationFriend: {
        respondent1LitigationFriend: {
          firstName: 'Bob',
          lastName: 'the litigant friend',
          emailAddress: 'bobthelitigant@litigants.com',
          phoneNumber: '07123456789',
          hasSameAddressAsLitigant: 'No',
          primaryAddress: buildAddress('litigant friend')
        }
      }
    }
  },
  ONE_V_TWO_ONE_LEGAL_REP: {
    valid: {
      DefendantLitigationFriend: {
        respondent1LitigationFriend: {
          firstName: 'Bob',
          lastName: 'the litigant friend',
          emailAddress: 'bobthelitigant@litigants.com',
          phoneNumber: '07123456789',
          hasSameAddressAsLitigant: 'Yes',
          certificateOfSuitability: [
            {
              id: docUuid,
              value: {
                document: {
                  document_url: '${TEST_DOCUMENT_URL}',
                  document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
                  document_filename: '${TEST_DOCUMENT_FILENAME}'
                }
              }
            }
          ]
        }
      }
    }
  },
  ONE_V_TWO_TWO_LEGAL_REP: {
    valid: {
      DefendantLitigationFriend: {
        respondent2LitigationFriend: {
          firstName: 'David',
          lastName: 'the litigant friend',
          emailAddress: 'davidthelitigant@litigants.com',
          phoneNumber: '07123458675',
          hasSameAddressAsLitigant: 'No',
          primaryAddress: buildAddress('litigant friend')
        }
      }
    }
  }
};
