const {I} = inject();

module.exports = {

  fields: respondent => {
    return {
      respondentRepresented: {
        id: `#${respondent}Represented`,
        options: {
          yes: `#${respondent}Represented_Yes`,
          no: `#${respondent}Represented_No`
        }
      }
    };
  },

  async enterRespondentRepresented(respondent, respondentRepresented) {
     
    I.waitForElement(this.fields(respondent).respondentRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondent).respondentRepresented.id, () => {
      const { yes, no } = this.fields(respondent).respondentRepresented.options;
      I.click(respondentRepresented ? yes : no);
    });

    await I.clickContinue();
  }
};

