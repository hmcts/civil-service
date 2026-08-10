const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      futureApplications: {
        id: `#${party}DQFutureApplications_intentionToMakeFutureApplications`,
        options: {
          yes: `#${party}DQFutureApplications_intentionToMakeFutureApplications_Yes`,
          no: `#${party}DQFutureApplications_intentionToMakeFutureApplications_No`
        }
      },
      reasonForFutureApplications: `#${party}DQFutureApplications_intentionToMakeFutureApplications`,
      otherInformationForJudge: `#${party}DQFutureApplications_otherInformationForJudge`,
    };
  },

  async enterFurtherInformation(party) {
    I.waitForElement(this.fields(party).futureApplications.id);
    await I.runAccessibilityTest();

    I.click(this.fields(party).futureApplications.options.no);
    await I.clickContinue();
  },
};
