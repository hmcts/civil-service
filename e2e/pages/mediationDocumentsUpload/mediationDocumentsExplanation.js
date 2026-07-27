const {I} = inject();

module.exports = {

  async uploadADocument() {
    await I.waitForText('About this service', 5);
    await I.see('Deadlines for uploading documents');
    await I.see('Before you upload your documents');
    await I.clickContinue();
  }
};
