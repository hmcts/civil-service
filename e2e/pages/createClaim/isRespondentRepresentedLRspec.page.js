const {I} = inject();

module.exports = {

  fields: {
    respondent1Represented: {
      id: '#specRespondent1Represented',
      options: {
        yes: '#specRespondent1Represented_Yes',
        no: '#specRespondent1Represented_No'
      }
    },
  },

  async enterRespondentRepresented(respondentRepresentedOption) {
     
    if (!this.fields.respondent1Represented.options.hasOwnProperty(respondentRepresentedOption)) {
      throw new Error(`Respondent represented option: ${respondentRepresentedOption} does not exist`);
    }
    I.waitForElement(this.fields.respondent1Represented.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondent1Represented.id, () => {
      I.click(this.fields.respondent1Represented.options[respondentRepresentedOption]);
    });

    await I.clickContinue();
  }
};

