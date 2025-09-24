const {I} = inject();

module.exports = {
  fields: {
    refundcontactinformation: {
      email: '//input[@id=\'email\']'
    }
  },

  async inputContactInformation() {
    I.wait(1);
    I.see('Process refund','h1');
    I.see('Case reference:','h2');
    I.see('Payment reference:');
    I.fillField(this.fields.refundcontactinformation.email,'test@hmcts.net');
    I.click('Continue');
  }
};
