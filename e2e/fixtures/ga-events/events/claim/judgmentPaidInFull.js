const {date} = require('../../../../api/dataHelper');

module.exports = {
  valid: {
    MarkJudgmentPaidInFull: {
      joJudgmentPaidInFull: {
        dateOfFullPaymentMade:  date(0),
        confirmFullPaymentMade:['CONFIRMED']
      }
    }
  }
};
