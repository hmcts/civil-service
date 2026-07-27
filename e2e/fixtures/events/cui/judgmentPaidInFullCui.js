const { date } = require('../../../api/dataHelper');

module.exports = {
  event: 'JUDGMENT_PAID_IN_FULL',
  caseDataUpdate: {
    joJudgmentPaidInFull: {
      dateOfFullPaymentMade: date(),
      confirmFullPaymentMade: [
        'CONFIRMED'
      ]
    }
  }
};
