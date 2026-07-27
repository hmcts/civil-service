const {I} = inject();

module.exports = {
  fields: {
    enterReasons: {
      id: '#responseToClaimAdmitPartWhyNotPayLRspec',
    },
  },

  async enterReasons() {
    await I.fillField(this.fields.enterReasons,'low income');
    await I.clickContinue();
  }
};
