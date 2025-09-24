const {I} = inject();

module.exports = {

  async verifyChangeYourAnswersPageAndChangeReason() {
    I.see('Check your answers');
    I.see('Payment reference');
    I.see('Reason for return');
    I.see('Refund reference');
    I.see('Reason for refund');
    I.see('Amended claim');
    I.see('Refund amount');
    I.see('£550');
    I.see('Send to');
    I.see('Send via');
    I.see('Email');
    I.see('test@hmcts.net');
    I.see('Notification');
    I.click('//tr[4]//a[.=\'Change\']');
  }
};
