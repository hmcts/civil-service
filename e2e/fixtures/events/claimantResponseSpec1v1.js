const {listElement, element} = require('../../api/dataHelper');
const config = require('../../config.js');

module.exports = {
  claimantResponse_JBA: (response = 'FA_ACCEPT_CCJ') => {
    const responseData = {
    };
    switch (response) {
      case 'FA_ACCEPT_CCJ':
        responseData.userInput = {
          ...responseData.userInput,
          RespondentResponse: {
            applicant1AcceptFullAdmitPaymentPlanSpec: 'Yes',
          },
          CcjPaymentPaidSome: {
            ccjPaymentPaidSomeOption: 'No',
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
          Mediation: {
            applicant1ClaimMediationSpecRequiredLip: {
              hasAgreedFreeMediation: 'No'
            }
          },
        };
        responseData.midEventData = {
        ...responseData.midEventData
        };
        break;

    }
    return responseData;
  }
};
