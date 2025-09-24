const {I} = inject();

module.exports = {
  fields: {
    servicerequest: {
      review_link: '//tr[2]//a[.=\'Review\']',
      issue_refund_button: '//button[.=\'Issue refund\']'
    }
  },


  async issueRefunds() {

    I.waitForText('Paid');
    I.waitForText('£550.00');//Making sure that the Service Request page is loaded properly before proceeding...
    I.click(this.fields.servicerequest.review_link); //As the Second payment in the test is Paid and rolledback.
    I.click(this.fields.servicerequest.issue_refund_button);
  }
};
