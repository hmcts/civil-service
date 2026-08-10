const {I} = inject();
const {waitForFinishedBusinessProcess, waitForGAFinishedBusinessProcess} = require('../../api/testingSupport');
const config = require('../../config');

module.exports = {

  fields: {
    confirmation: {
      id: '#confirmation-body'
    },
    applicationFeeLink: '#confirmation-body a',
    feeTextInfo: '#confirmation-body p:nth-child(2)',
    paymentNavigationInfo: '#confirmation-body p:nth-child(4)'
  },

  async verifyConfirmationPage(parentCaseId, consentCheck, notice, type) {
    let appType = type.toString().trim();
    console.log('The application type : ' + appType);
    let fee;
    if (('no' === consentCheck && 'yes' === notice) && ('Vary payment terms of judgment' !== appType || 'Vary order' !== appType)) {
      fee = '£313.00';
    } else if ('Vary payment terms of judgment' === appType || 'Vary order' === appType) {
      fee = '£15.00';
    } else if ('Strike out,Summary judgment,Stay the claim,Extend time' === appType || 'Extend time' === appType){
      fee = '£123.00';
    } else {
      fee = '£123.00';
    }

    let confirmation_msg = `Your application fee of ${fee} is now due for payment. Your application will not be processed further until this fee is paid.`;
    I.waitForElement(this.fields.confirmation.id);
    I.seeInCurrentUrl('INITIATE_GENERAL_APPLICATION/confirm');
    await I.seeTextEquals(confirmation_msg, this.fields.feeTextInfo);
    I.seeTextEquals('To pay this fee, click the link below, or else open your application from the Applications tab of this case listing and then click on the service request tab.', this.fields.paymentNavigationInfo);
    I.seeTextEquals('Pay your application fee', this.fields.applicationFeeLink);
    await waitForFinishedBusinessProcess(parentCaseId, config.applicantSolicitorUser);
    await waitForGAFinishedBusinessProcess(parentCaseId, config.applicantSolicitorUser);
  },

  async closeAndReturnToCaseDetails() {
    await I.click('Close and Return to case details');
    await I.see('Make an application');
  },

  async clickPayFeeLink() {
    await I.forceClick(this.fields.applicationFeeLink);
    await I.wait(5);
    await I.switchToNextTab();
    await I.closeOtherTabs();
  }
};

