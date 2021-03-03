const {date} = require('../../api/dataHelper');

module.exports = {
  valid: {
    ExtensionDate: {
      respondentSolicitor1AgreedDeadlineExtension: date(40)
    }
  },
  invalid: {
    ExtensionDate: {
      past: {
        respondentSolicitor1AgreedDeadlineExtension: date(-1)
      },
      beforeCurrentDeadline: {
        respondentSolicitor1AgreedDeadlineExtension: date(10)
      }
    }
  }
};
