const { listElement } = require('../../api/dataHelper');

const selectedPBA = listElement('PBAFUNC12345');
module.exports = {
  valid: {
    ResubmitClaim: {
      applicantSolicitor1PbaAccounts: {
        list_items: [
          selectedPBA,
          listElement('PBA0078095')
        ],
        value: selectedPBA
      }
    }
  }
};
