const {date} = require('../../api/dataHelper');

module.exports = {
  valid: {
    ProposeDeadline: {
      respondentSolicitor1claimResponseExtensionProposedDeadline: date(40)
    },
    ExtensionAlreadyAgreed: {
      respondentSolicitor1claimResponseExtensionAlreadyAgreed: 'No',
      respondentSolicitor1claimResponseExtensionReason: 'Test reason'
    }
  },
  invalid: {
    ProposeDeadline: {
      past: {
        respondentSolicitor1claimResponseExtensionProposedDeadline: date(-1)
      },
      beforeCurrentDeadline: {
        respondentSolicitor1claimResponseExtensionProposedDeadline: date(10)
      }
    }
  }
};
