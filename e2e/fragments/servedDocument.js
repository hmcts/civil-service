const {I} = inject();

module.exports = {

  async upload(file, documents) {
    await I.runAccessibilityTest();
    for (const fileType of documents) {
      await within(fileType, async () => {
        I.click('Add new');
        if (fileType == '#servedDocumentFiles_certificateOfSuitability' || fileType == '#servedDocumentFiles_scheduleOfLoss') {
          await I.attachFile(fileType + '_0_document', file);
        }else{
          await I.attachFile(fileType + '_value', file);
        }
        await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
      });
    }
  },
};

