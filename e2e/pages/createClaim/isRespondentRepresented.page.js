const {I} = inject();

module.exports = {

  fields: {
    respondent1Represented: {
      id: '#respondent1Represented',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
  },

  async enterRespondentRepresented(respondentRepresentedOption) {
    // eslint-disable-next-line no-prototype-builtins
    if (!this.fields.respondent1Represented.options.hasOwnProperty(respondentRepresentedOption)) {
      throw new Error(`Respondent represented option: ${respondentRepresentedOption} does not exist`);
    }
    I.waitForElement(this.fields.respondent1Represented.id);
    await within(this.fields.respondent1Represented.id, () => {
      I.click(this.fields.respondent1Represented.options[respondentRepresentedOption]);
    });

    await I.clickContinue();
  }
};

