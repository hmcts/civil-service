const {I} = inject();

module.exports = {
  fields: {
    refundoptions: {
      organisation: '[name=\'organisation\']'
    }
  },


  async chooseRefunds() {

    I.wait(3);
    I.waitForText('Money Claims - Claim Amount - 10000.01 up to 200000 GBP. FEE AMOUNT = 5% of claim value');
    I.see('Process refund','h1');
    I.see('Case reference:','h2');
    I.see('Payment reference:');
    I.see('Select fees to be refunded');
    I.see('Fee description');
    I.see('Fee amount');
    I.see('Total paid');
    I.see('Quantity');
    I.see('Amount to refund');
    I.see('£550.00');
    I.click(this.fields.refundoptions.organisation);
    I.click('Continue');
  }
};
