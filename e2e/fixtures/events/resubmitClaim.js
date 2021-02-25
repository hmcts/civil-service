const { listElement } = require('../../api/dataHelper');

const selectedPBA = listElement('PBA0077597');
module.exports = {
  valid: {
    ResubmitClaim: {
      applicantSolicitor1PbaAccounts: {
        list_items: [
          selectedPBA,
          listElement('PBA0078094')
        ],
        value: selectedPBA
      },
      paymentReference: 'Updated reference'
    },
  }
};
