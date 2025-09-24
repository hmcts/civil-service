const {I} = inject();

module.exports = {

  async verifyRefundsDetailsAndInitiateChange() {

    I.waitForText('Sent for approval',3);
    I.see('Refund details', 'h2');
    I.see('Refund reference');
    I.see('Payment to be refunded');
    I.see('Reason for refund');
    I.see('Amended claim');
    I.see('Amount refunded');
    I.see('£550.00');

    I.see('Notifications sent','h2');
    I.see('Date and time');
    I.see('Sent to');
    I.see('Sent via');
    I.see('Actions');


    I.see('Refund status history','h2');
    I.see('Status');
    I.see('Date and time');
    I.see('Users');
    I.see('Notes');
    I.see('Update required');
    I.see('Sent for approval');
    I.click('Change refund details');
  }
};
