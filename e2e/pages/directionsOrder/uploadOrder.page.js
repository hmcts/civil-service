const { I } = inject();

module.exports = {
  fields: {
    label1: '[id="uploadOrderDocumentFromTemplateLabel"] h2',
    label2: '[id="uploadOrderDocumentFromTemplateLabel"] p',
    uploadInput: '#uploadOrderDocumentFromTemplate',
  },

  async verifyLabelsAndUploadDocument(filePath) {
    I.waitForElement(this.fields.label1);
    I.see('Add document', this.fields.label1);

    I.seeElement(this.fields.label2);
    I.see('Upload your completed order in DOC/DOCX (Word) format.', this.fields.label2);

    I.attachFile(this.fields.uploadInput, filePath);
    I.wait(5);
    // Use JavaScript to check if the file was uploaded by grabbing the file input's value
    const uploadedFile = await I.executeScript(() => {
      const inputFileElement = document.querySelector('#uploadOrderDocumentFromTemplate');
      return inputFileElement ? inputFileElement.value : null;
    });

    // Assert that the file was uploaded and its name matches
    if (!uploadedFile || !uploadedFile.includes('exampleDOC.docx')) {
      throw new Error('File was not uploaded or the file name does not match.');
    }

    await I.clickContinue();
  }
};
