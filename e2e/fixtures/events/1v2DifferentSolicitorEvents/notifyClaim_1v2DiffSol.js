const { listElement } = require('../../../api/dataHelper');

const selectedNotifyOption = listElement('Both');

module.exports = {
  valid: {
    validateNotificationOption:{
      defendantSolicitorNotifyClaimOptions: {
        list_items: [
          selectedNotifyOption,
          listElement('Defendant One: Sir John Doe'),
          listElement('Defendant Two: Dr Foo Bar')
        ],
        value: selectedNotifyOption
      }
    }
  }
};
