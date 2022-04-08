const {I} = inject();

module.exports = {
  fields: {
    hearingType: {
      id: '#respondent1DQHearingSmallClaim_unavailableDatesRequired_radio',
      options: {
        yes: 'Yes',
        no: 'No',
      }
    },
    interpreterType: {
          id: '#SmallClaimHearingInterpreterRequired_radio',
          options: {
            yes: 'Yes',
            no: 'No',
          }
     }
  },

  async selectHearing(responseType) {

    I.waitForElement(this.fields.hearingType.id);
    await I.runAccessibilityTest();
    await within(this.fields.hearingType.id, () => {
    I.click(this.fields.hearingType.options[responseType]);
    I.click(this.fields.interpreterType.options[responseType]);
    });

    I.waitForElement(this.fields.interpreterType.id);
    await I.runAccessibilityTest();
    await within(this.fields.interpreterType.id, () => {
    I.click(this.fields.interpreterType.options[responseType]);
    });
    await I.clickContinue();
  }
};

