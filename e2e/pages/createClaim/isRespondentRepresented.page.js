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

  async enterRespondentRepresented(respondent, respondentRepresented) {
    // eslint-disable-next-line no-prototype-builtins
    I.waitForElement(this.fields(respondent).respondentRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondent).respondentRepresented.id, () => {
      const { yes, no } = this.fields(respondent).respondentRepresented.options;
      I.click(respondentRepresented ? yes : no);
    });

    await I.clickContinue();
  }
};

