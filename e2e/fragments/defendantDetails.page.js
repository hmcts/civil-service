const {I} = inject();

module.exports = {

  fields: {
    defendantDetails: {
      id: '#defendantDetails'
    }
  },

  async verifyDetails() {
    I.waitForElement(this.fields.defendantDetails.id);
    await I.see('Example company');

    await I.clickContinue();
  }
};

