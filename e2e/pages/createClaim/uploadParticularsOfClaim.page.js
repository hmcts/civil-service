const {I} = inject();

const servedDocuments = require('../../fragments/servedDocument');

module.exports = {

  fields: {
    servedDocumentFiles: {
      particularsOfClaim: '#servedDocumentFiles_particularsOfClaimDocument',
      options: [
        '#servedDocumentFiles_scheduleOfLoss',
        '#servedDocumentFiles_certificateOfSuitability',
      ]
    }
  },

  async upload(file) {
    I.waitForElement(this.fields.servedDocumentFiles.particularsOfClaim);
    await I.attachFile(this.fields.servedDocumentFiles.particularsOfClaim, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    await servedDocuments.upload(file, this.fields.servedDocumentFiles.options);

    await I.clickContinue();
  },
};

