module.exports = {

  judgeDecisionOnReconsiderationRequestSpec : (decisionSelection) => {
    if (decisionSelection == 'YES') {
      return {
        userInput: judgeDecisionOnReconsiderationRequestYes()
      };
    }
    else if (decisionSelection == 'CREATE_SDO') {
      return {
        userInput: judgeDecisionOnReconsiderationRequestCreateSDO()
      };
    }
    else {
      return {
        userInput: judgeDecisionOnReconsiderationRequestCreateGeneralOrder()
      };
    }
  }
};

const judgeDecisionOnReconsiderationRequestYes = () => {
  return  {
    JudgeResponseToReconsideration: {
      decisionOnRequestReconsiderationOptions: 'YES',
      upholdingPreviousOrderReason: {
        reasonForReconsiderationTxtYes: 'Having read the application for reconsideration of the Legal Advisor\'s order dated 29 November 2023 and the court file \n 1.The application for reconsideration of the order is dismissed.'
      }
    }
  };
};

const judgeDecisionOnReconsiderationRequestCreateSDO = () => {
  return  {
    JudgeResponseToReconsideration: {
      decisionOnRequestReconsiderationOptions: 'CREATE_SDO'
    }
  };
};

const judgeDecisionOnReconsiderationRequestCreateGeneralOrder = () => {
  return  {
    JudgeResponseToReconsideration: {
      decisionOnRequestReconsiderationOptions: 'CREATE_GENERAL_ORDER'
    }
  };
};
