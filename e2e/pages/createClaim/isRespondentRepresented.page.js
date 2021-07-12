const {I} = inject();

module.exports = {

  fields: respondent => {
    return {
      respondentRepresented: {
        id: `#${respondent}Represented`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      }
    };
  },

  async enterRespondentRepresented(respondent, respondentRepresentedOption) {
    // eslint-disable-next-line no-prototype-builtins
    if (!this.fields(respondent).respondentRepresented.options.hasOwnProperty(respondentRepresentedOption)) {
      throw new Error(`Respondent represented option: ${respondentRepresentedOption} does not exist`);
    }
    I.waitForElement(this.fields(respondent).respondentRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondent).respondentRepresented.id, () => {
      I.click(this.fields(respondent).respondentRepresented.options[respondentRepresentedOption]);
    });

    await I.clickContinue();
  }
};

