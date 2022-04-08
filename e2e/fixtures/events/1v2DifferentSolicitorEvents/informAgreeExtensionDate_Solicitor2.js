const {date} = require('../../../api/dataHelper');

module.exports = {
  valid: {
    ExtensionDate: {
      respondentSolicitor2AgreedDeadlineExtension: date(40)
    }
  },
  invalid: {
    ExtensionDate: {
      past: {
        respondentSolicitor2AgreedDeadlineExtension: date(-1)
      },
      beforeCurrentDeadline: {
        respondentSolicitor2AgreedDeadlineExtension: date(10)
      }
    }
  }
};
