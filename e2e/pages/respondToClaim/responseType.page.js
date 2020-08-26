const {I} = inject();

module.exports = {

  fields: {
    respondent1ClaimResponseType: {
      id: '#respondent1ClaimResponseType',
      options: {
        fullDefence: 'Rejects all of the claim',
        fullAdmission: 'Admits all of the claim',
        partAdmission: 'Admits part of the claim',
        counterClaim: 'Reject all of the claim and wants to counterclaim'
      }
    }
  },

  async selectFullDefence() {
    I.waitForElement(this.fields.respondent1ClaimResponseType.id);
    await within(this.fields.respondent1ClaimResponseType.id, () => {
      I.click(this.fields.respondent1ClaimResponseType.options.fullDefence);
    });

    await I.clickContinue();
  }
};

