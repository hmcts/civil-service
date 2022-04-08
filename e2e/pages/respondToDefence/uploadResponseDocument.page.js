const { I } = inject();

module.exports = {

  fields: function (mpScenario) {
    return {
      applicantResponseDocument: {
        id: '#applicant1DefenceResponseDocument_file'
      },
      ...(mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP') ?
        {
          applicantResponseDocumentAgainstDefendant2:{
            id: '#claimantDefenceResDocToDefendant2_file'
          }
        } : {}
    };
  },

  async uploadResponseDocuments (file, mpScenario) {
    I.waitForElement(this.fields(mpScenario).applicantResponseDocument.id);
    await I.runAccessibilityTest();
    await I.attachFile(this.fields(mpScenario).applicantResponseDocument.id, file);
    await I.waitForInvisible(locate('.error-message').withText('Uploading...'));

    if(mpScenario === 'ONE_V_TWO_TWO_LEGAL_REP'){
      await I.attachFile(this.fields(mpScenario).applicantResponseDocumentAgainstDefendant2.id, file);
      await I.waitForInvisible(locate('.error-message').withText('Uploading...'));
    }
    await I.clickContinue();
  },
};

