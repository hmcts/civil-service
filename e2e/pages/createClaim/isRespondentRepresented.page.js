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

  async enterRespondentRepresented() {
    I.waitForElement(this.fields.respondent1Represented.id);
    await within(this.fields.respondent1Represented.id, () => {
      I.click(this.fields.respondent1Represented.options.yes);
    });

    await I.clickContinue();
  }
};

