const {I} = inject();

module.exports = {
  async checkYourAnswers(changeReason = false) {
    I.wait(1);
    I.see('Payment reference');
      I.see('£550');
      I.see('Reason for refund');
      if (!changeReason) {
        I.see('Amended claim');
        I.see('Payment amount');
      } else {
        I.see('System/technical error');
      }
      I.see('£550');
      I.see('Refund amount');
      I.see('Send via');
      I.see('Email');
      I.see('test@hmcts.net');
      I.click('Submit refund');
    }
  };
