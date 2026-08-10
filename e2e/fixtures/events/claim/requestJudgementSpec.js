module.exports = {
  createRequestJudgment: (response = 'REQUEST_JUDGEMENT') => {
    const responseData = {
    };
    switch (response) {
      case 'REQUEST_JUDGEMENT':
        responseData.userInput = {
          ...responseData.userInput,
          CcjPaymentPaidSome: {
            ccjPaymentPaidSomeOption: 'Yes',
            ccjPaymentPaidSomeAmount: '1000',
          },
          FixedCost: {
            ccjJudgmentFixedCostOption: 'Yes',
          },
          CcjJudgmentSummary: {
            ccjJudgmentAmountClaimAmount: '1000',
            ccjJudgmentAmountInterestToDate: '35',
            ccjJudgmentAmountClaimFee: '100',
            ccjJudgmentFixedCostAmount: '40',
            ccjJudgmentAmountSubtotal: '1175',
            ccjPaymentPaidSomeAmountInPounds: '10',
            ccjJudgmentTotalStillOwed: '1165',
            ccjJudgmentStatement: 'test'
          },
        };
        responseData.midEventData = {
          ...responseData.midEventData,
        };
        break;
    }
    return responseData;
  }
};
