const {I} = inject();
module.exports = {

  async uploadSupportingFile(eventID, file) {
    I.seeInCurrentUrl(eventID);
    I.see('Response file');
    I.click('Add new');
    switch (eventID) {
      case 'RESPOND_TO_JUDGE_WRITTEN_REPRESENTATION':
        await I.see('Upload a file in response to Judge\'s written representations');
        await I.attachFile('#generalAppWrittenRepUpload' + '_value', file);
        break;
      case 'RESPOND_TO_JUDGE_DIRECTIONS':
        await I.see('Upload a file in response to Judge\'s directions');
        await I.attachFile('#generalAppDirOrderUpload' + '_value', file);
        break;
      case 'RESPOND_TO_JUDGE_ADDITIONAL_INFO':
        await I.see('Upload a file in response to Judge\'s additional information');
        await I.attachFile('#generalAppAddlnInfoUpload' + '_value', file);
        break;
    }
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
    await I.clickContinue();
  }
};
