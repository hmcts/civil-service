const {I} = inject();

module.exports = {

  fields: {
    respondent2SameLegalRepresentative: {
      id: '#respondent2SameLegalRepresentative',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterRespondent2SameLegalRepresentative() {
    I.waitForElement(this.fields.respondent2SameLegalRepresentative.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondent2SameLegalRepresentative.id, () => {
      I.click(this.fields.respondent2SameLegalRepresentative.options.no);
    });

    await I.clickContinue();
  }
};

