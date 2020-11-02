const {date} = require('../../api/dataHelper');

module.exports = {
  valid: {
    Respond: {
      respondentSolicitor1claimResponseExtensionAccepted: 'No'
    },
    Counter: {
      respondentSolicitor1claimResponseExtensionCounter: 'Yes',
      respondentSolicitor1claimResponseExtensionCounterDate: date(40)
    },
    Reason: {
      respondentSolicitor1claimResponseExtensionRejectionReason: 'Test rejection reason'
    }
  },
  invalid: {
    Counter: {
      past: {
        respondentSolicitor1claimResponseExtensionCounter: 'Yes',
        respondentSolicitor1claimResponseExtensionCounterDate: date(-1)
      },
      beforeCurrentDeadline: {
        respondentSolicitor1claimResponseExtensionCounter: 'Yes',
        respondentSolicitor1claimResponseExtensionCounterDate: date(10)
      }
    }
  }
};
