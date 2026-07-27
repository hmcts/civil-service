const { date } = require('../../api/dataHelper');


module.exports = {
  valid: {
    CaseProceedsInCaseman: {
      claimProceedsInCasemanLR: {
        date: date(-1),
        reason: 'APPLICATION',
      }
    }
  },
  invalid: {
    CaseProceedsInCaseman: {
      claimProceedsInCasemanLR: {
        date: date(1),
        reason: 'APPLICATION',
      }
    }
  }
};
