const {I} = inject();

module.exports = {

  fields: {
    servedDocumentFiles: {
      id: '#servedDocumentFiles_servedDocumentFiles',
      options: [
        '#servedDocumentFiles_particularsOfClaim',
        '#servedDocumentFiles_medicalReports',
        '#servedDocumentFiles_scheduleOfLoss',
        '#servedDocumentFiles_certificateOfSuitability',
        '#servedDocumentFiles_other'
      ]
    }
  },

  async uploadServedDocuments(file) {
    I.waitForElement(this.fields.servedDocumentFiles.id);
    for (const fileType of this.fields.servedDocumentFiles.options) {
      await within(fileType, async () => {
        I.click('Add new');
        await I.attachFile(fileType + '_0', file);
        await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
      });
    }

    await I.clickContinue();
  },
};

