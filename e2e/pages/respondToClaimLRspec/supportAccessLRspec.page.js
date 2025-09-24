const {I} = inject();

module.exports = {
  fields: {
    supportAccess: {
      id: '#respondent1DQHearingSupport_supportRequirements_radio',
      options: {
        yes: '#respondent1DQHearingSupport_supportRequirements_Yes',
        no: '#respondent1DQHearingSupport_supportRequirements_No',
      },
      id2: '#respondent2DQHearingSupport_supportRequirements_radio',
      id2options: {
        yes: '#respondent2DQHearingSupport_supportRequirements_Yes',
        no: '#respondent2DQHearingSupport_supportRequirements_No',
      }
    }
  },

  async selectSupportAccess(responseType,twoDefendants) {
   if(!twoDefendants){
      I.waitForElement(this.fields.supportAccess.id);
      await I.runAccessibilityTest();
      I.click(this.fields.supportAccess.options[responseType]);
   } else {
       I.waitForElement(this.fields.supportAccess.id2);
       await I.runAccessibilityTest();
       I.click(this.fields.supportAccess.id2options[responseType]);
   }
    await I.clickContinue();
  }
};

