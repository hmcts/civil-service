const { date } = require('../../api/dataHelper');


module.exports = {
  valid: {
    CaseProceedsInCaseman: {
      claimProceedsInCaseman: {
        date: date(-1),
        reason: 'APPLICATION',
      }
    }
  },
  invalid: {
    CaseProceedsInCaseman: {
      claimProceedsInCaseman: {
        date: date(1),
        reason: 'APPLICATION',
      }
    }
  }
};
