const {I} = inject();

module.exports = {
  fields: {
    refunddetails: {
      refund_action_for_return: '#refundAction-2',
      refund_notes_for_return: '#sendmeback'
    }
  },


  async verifyAndProcessRefundsDetails(action = 'Return to caseworker', reasonForRefund = 'Amended claim') {

    I.see('Review refund details', 'h1');
    I.see('Payment to be refunded');
    I.see('Reason for refund');
    I.see(`${reasonForRefund}`);
    I.see('Amount to be refunded');
    I.see('£550.00');
    I.see('Sent to');
    I.see('Sent via');
    I.see('Email');
    I.see('test@hmcts.net');
    I.see('Submitted by');
    I.see('Date submitted');
    I.see('Notification');

    I.waitForText('Some information needs correcting');
    I.see('What do you want to do with this refund?');
    I.see('Approve');
    I.see('Send to middle office');
    I.see('Reject');
    I.see('There is no refund due');
    I.see('Return to caseworker');

    if (action === 'Return to caseworker') {

      I.click(this.fields.refunddetails.refund_action_for_return);
      I.fillField(this.fields.refunddetails.refund_notes_for_return, 'Automation Test Comments');
      I.click('Submit');
      I.waitForText('Refund returned to caseworker');

    } else if (action === 'Approve') {

      I.click('#refundAction-0');
      I.click('Submit');
      I.waitForText('Refund approved');

    } else if (action === 'Reject') {
      I.click('#refundAction-1');
      I.waitForText('Other');
      I.click('#refundRejectReason-0');
      I.click('Submit');

      I.waitForText('Refund rejected');
    }
  }
};
