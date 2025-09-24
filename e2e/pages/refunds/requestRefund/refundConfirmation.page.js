const {I} = inject();

module.exports = {
  async verifyConfirmationPage() {
    I.waitForText('A refund request for £550.00 has been created and will be passed to a team leader to approve.');
    I.wait(1);
    I.see('Refund submitted','h1');
    I.see('Refund reference:');
  }
};
