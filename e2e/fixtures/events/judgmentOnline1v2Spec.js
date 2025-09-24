const {date} = require('../../api/dataHelper');

module.exports = {
  recordJudgment: (whyRecorded, paymentPlanSelection) => {
    const recordJudgment = {
    };
    switch (paymentPlanSelection) {
      case 'PAY_IN_INSTALMENTS': {
        if (whyRecorded === 'DETERMINATION_OF_MEANS') {
          recordJudgment.userInput = {
            ...recordJudgment.userInput,
            RecordJudgment: {
              joJudgmentRecordReason: 'DETERMINATION_OF_MEANS',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IN_INSTALMENTS'
              },
              joInstalmentDetails: {
                amount: '10000',
                paymentFrequency: 'WEEKLY',
                startDate: date(1)
              },
              joIsRegisteredWithRTL: 'Yes',
              addRespondent2: 'Yes',
            },
          };
        } else if (whyRecorded === 'JUDGE_ORDER') {
          recordJudgment.userInput = {
            ...recordJudgment.userInput,
            RecordJudgment: {
              joJudgmentRecordReason: 'JUDGE_ORDER',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IN_INSTALMENTS'
              },
              joInstalmentDetails: {
                amount: '10000',
                paymentFrequency: 'WEEKLY',
                startDate: date(1)
              },
              joIsRegisteredWithRTL: 'Yes',
              addRespondent2: 'Yes',
            },
          };
        }
      }
      break;
      case 'PAY_IMMEDIATELY':{
        if (whyRecorded === 'DETERMINATION_OF_MEANS') {
          recordJudgment.userInput = {
            ...recordJudgment.userInput,
            RecordJudgment: {
              joJudgmentRecordReason: 'DETERMINATION_OF_MEANS',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IMMEDIATELY'
              },
              joIsRegisteredWithRTL: 'No',
              addRespondent2: 'Yes'
            },
          };
        } else if (whyRecorded === 'JUDGE_ORDER') {
          recordJudgment.userInput = {
            ...recordJudgment.userInput,
            RecordJudgment: {
              joJudgmentRecordReason: 'JUDGE_ORDER',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IMMEDIATELY'
              },
              joIsRegisteredWithRTL: 'Yes',
              addRespondent2: 'Yes'
            },
          };
        }
      }
      break;
    }
    return recordJudgment;
  },
  editJudgment: (whyRecorded, paymentPlanSelection) => {
    const editJudgment = {
    };
    switch (paymentPlanSelection) {
      case 'PAY_IN_INSTALMENTS': {
        if (whyRecorded === 'DETERMINATION_OF_MEANS') {
          editJudgment.userInput = {
            ...editJudgment.userInput,
            EditJudgment: {
              joJudgmentRecordReason: 'DETERMINATION_OF_MEANS',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IN_INSTALMENTS'
              },
              joInstalmentDetails: {
                amount: '10000',
                paymentFrequency: 'WEEKLY',
                startDate: date(1)
              },
              joIsRegisteredWithRTL: 'Yes',
              addRespondent2: 'Yes',
            },
          };
        } else if (whyRecorded === 'JUDGE_ORDER') {
          editJudgment.userInput = {
            ...editJudgment.userInput,
            EditJudgment: {
              joJudgmentRecordReason: 'JUDGE_ORDER',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_IN_INSTALMENTS'
              },
              joInstalmentDetails: {
                amount: '10000',
                paymentFrequency: 'WEEKLY',
                startDate: date(1)
              },
              joIsRegisteredWithRTL: 'Yes',
              addRespondent2: 'Yes',
            },
          };
        }
      }
      break;
      case 'PAY_BY_DATE':{
        if (whyRecorded === 'DETERMINATION_OF_MEANS') {
          editJudgment.userInput = {
            ...editJudgment.userInput,
            EditJudgment: {
              joJudgmentRecordReason: 'DETERMINATION_OF_MEANS',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_BY_DATE',
                paymentDeadlineDate: date(1)
              },
              addRespondent2: 'Yes'
            },
          };
        } else if (whyRecorded === 'JUDGE_ORDER') {
          editJudgment.userInput = {
            ...editJudgment.userInput,
            RecordJudgment: {
              joJudgmentRecordReason: 'JUDGE_ORDER',
              joOrderMadeDate: date(-1),
              joAmountOrdered: '40000',
              joAmountCostOrdered: '20000',
              joPaymentPlan: {
                type: 'PAY_BY_DATE',
                paymentDeadlineDate: date(1)
              },
              addRespondent2: 'Yes'
            },
          };
        }
      }
      break;
    }
    return editJudgment;
  }
};
