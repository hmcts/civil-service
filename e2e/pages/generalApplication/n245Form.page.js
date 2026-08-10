const {I} = inject();

module.exports = {
  fields: {
    generalAppN245DownloadLink: '#generalAppN245Download a',
    n245DocumentFiles: '#generalAppN245FormUpload',
  },

  async uploadN245Form(file) {
    await I.waitInUrl('INITIATE_GENERAL_APPLICATIONGAUploadN245Form');
    await I.see('Upload your completed N245 form');
    await I.seeNumberOfVisibleElements(this.fields.generalAppN245DownloadLink, 1);
    await I.attachFile(this.fields.n245DocumentFiles, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
    await I.wait(5);
    await I.clickContinue();
  }
};
