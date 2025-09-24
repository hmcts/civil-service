const {date} = require('../../../api/dataHelper');

module.exports = {
  markJudgmentPaidInFull: () => {
    const markJudgmentPaid = {
      event: 'JUDGMENT_PAID_IN_FULL',
      caseDataUpdate: {
        joJudgmentPaidInFull: {
          dateOfFullPaymentMade:  date(0),
          confirmFullPaymentMade:['CONFIRMED']
        }
      }
    };
    return markJudgmentPaid;
  },
};
