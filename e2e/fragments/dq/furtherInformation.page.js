const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      futureApplications: {
        id: `#${party}DQFurtherInformation_futureApplications`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      reasonForFutureApplications: `#${party}DQFurtherInformation_reasonForFutureApplications`,
      otherInformationForJudge: `#${party}DQFurtherInformation_otherInformationForJudge`,
    };
  },

  async enterFurtherInformation(party) {
    I.waitForElement(this.fields(party).futureApplications.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).futureApplications.id, () => {
      I.click(this.fields(party).futureApplications.options.yes);
    });
    I.fillField(this.fields(party).reasonForFutureApplications, 'Reason for future applications');
    I.fillField(this.fields(party).otherInformationForJudge, 'Other information for judge');
    await I.clickContinue();
  },
};
