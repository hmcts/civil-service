const {I} = inject();

module.exports = {

  async upload(file, documents) {
    for (const fileType of documents) {
      await within(fileType, async () => {
        I.click('Add new');
        await I.attachFile(fileType + '_0_document', file);
        await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
      });
    }
  },
};

