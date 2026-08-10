const {I} = inject();
module.exports = {
  fields: {
    id: '#responseToClaimAdmitPartWhyNotPayLRspec',
  },
  async enterReasons() {
    await I.fillField(this.fields.id,'low income');
    await I.clickContinue();
   }
};
