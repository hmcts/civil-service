const {I} = inject();

const servedDocuments = require('../../fragments/servedDocument');

module.exports = {

  fields: {
    particularsOfClaimText: '#servedDocumentFiles_particularsOfClaimText',
    servedDocumentFiles: {
      options: [
        '#servedDocumentFiles_particularsOfClaimDocument'
      ]
    }
  },

  async upload(file) {
    await I.waitForElement(this.fields.servedDocumentFiles.options[0]);
    await I.runAccessibilityTest();
    await servedDocuments.upload(file, this.fields.servedDocumentFiles.options);

    await I.wait(5);
    await I.clickContinue();
  },

  async enterParticularsOfClaim() {
    await I.waitForElement(this.fields.particularsOfClaimText);
    await I.runAccessibilityTest();
    await I.fillField(this.fields.particularsOfClaimText, 'Particulars of claim description');
    await I.clickContinue();
  }
};

