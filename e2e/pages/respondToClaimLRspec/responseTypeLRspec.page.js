const {I} = inject();

module.exports = {

  fields: {
    respondent1ClaimResponseType: {
      id: '#respondent1ClaimResponseTypeForSpec',
      options: {
        fullDefence: 'Defends all of the claim',
        fullAdmission: 'Admits all of the claim',
        partAdmission: 'Admits part of the claim',
        counterClaim: 'Defends all of the claim and wants to counterclaim'
      }
    }
  },

  async selectResponseType(responseType) {
    // eslint-disable-next-line no-prototype-builtins
    if (!this.fields.respondent1ClaimResponseType.options.hasOwnProperty(responseType)) {
      throw new Error(`Response type: ${responseType} does not exist`);
    }
    I.waitForElement(this.fields.respondent1ClaimResponseType.id);
    await I.runAccessibilityTest();
    await within(this.fields.respondent1ClaimResponseType.id, () => {
    I.click(this.fields.respondent1ClaimResponseType.options[responseType]);
    });

   await I.clickContinue();
  }
};

