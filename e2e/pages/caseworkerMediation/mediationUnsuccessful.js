const {I} = inject();
module.exports = {
  fields: function() {
        return {
          mediationFailureReason: {
            id: '#mediationUnsuccessfulReasonsMultiSelect',
            options: {
              one: 'Party withdraws from mediation',
              two: 'Appointment no agreement reached',
              three: 'Appointment not assigned'
            }
          },
        };
  },


 async selectMediationFailureReason() {

    I.waitForElement(this.fields().mediationFailureReason.id);
    await I.runAccessibilityTest();
    await within(this.fields().mediationFailureReason.id, () => {
      I.click(this.fields().mediationFailureReason.options.one);
    });
    await I.clickContinue();
  }
};

