const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      expertRequired: {
        id: `#${party}DQExperts_expertRequired`,
        options: {
          yes: `#${party}DQExperts_expertRequired_Yes`,
          no: `#${party}DQExperts_expertRequired_No`
        }
      },
      expertReportsSent: {
        id: `#${party}DQExperts_expertReportsSent`,
        options: {
          yes: `#${party}DQExperts_expertReportsSent_Yes`,
          no: `#${party}DQExperts_expertReportsSent_No`
        }
      },
      jointExpertSuitable: {
        id: `#${party}DQExperts_jointExpertSuitable`,
        options: {
          yes: `#${party}DQExperts_jointExpertSuitable_Yes`,
          no: `#${party}DQExperts_jointExpertSuitable_No`
        }
      },
      expertDetails: {
        id: `#${party}DQExperts_details'`,
        element: {
          name: `#${party}DQExperts_details_0_name`,
          fieldOfExpertise: `#${party}DQExperts_details_0_fieldOfExpertise`,
          whyRequired: `#${party}DQExperts_details_0_whyRequired`,
          estimatedCost: `#${party}DQExperts_details_0_estimatedCost`,
        }
      }
    };
  },

  async enterExpertInformation(party) {
    I.waitForElement(this.fields(party).expertRequired.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).expertRequired.id, () => {
      I.click(this.fields(party).expertRequired.options.no);
    });

    await I.clickContinue();

  },

};
