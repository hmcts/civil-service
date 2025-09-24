const {listElement} = require('../../api/dataHelper');
const config = require('../../config.js');

const changeLocation = () => {
  return {
    NotSuitableSDO: {
      notSuitableSdoOptions: 'CHANGE_LOCATION',
      reasonNotSuitableSDO:{
        input: 'Other reason for not suitable SDO'
      },
      tocTransferCaseReason: {
        reasonForCaseTransferJudgeTxt: 'Reason for transferring case'
      },
    }
  };
};

const otherReasons = () => {
  return {
    TransferCase: {
      notSuitableSdoOptions: 'OTHER_REASONS',
      reasonNotSuitableSDO:{
        input: 'Other reason for not suitable SDO'
      }
    }
  };
};

module.exports = {
  notSuitableSDO : (option) => {
    if (option === 'CHANGE_LOCATION') {
      return {
        valid: changeLocation()
      };
    } else {
      return {
        valid: otherReasons()
      };
    }
  },

  transferCase : () => {
      return {
        valid: {
          transferCourtLocationList: {
            transferCourtLocationList: {
              value: listElement(config.liverpoolCourt),
              list_items: [listElement(config.claimantSelectedCourt)]
            },
            reasonForTransfer: 'Allocated court location is not appropriate'
          },
        }
      };
    }
};
