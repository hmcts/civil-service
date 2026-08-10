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
          yes: `#${party}DQExperts_expertReportsSent-YES`,
          no: `#${party}DQExperts_expertReportsSent-NO`
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
        oldElements: {
          name: `#${party}DQExperts_details_0_name`,
          fieldOfExpertise: `#${party}DQExperts_details_0_fieldOfExpertise`,
          whyRequired: `#${party}DQExperts_details_0_whyRequired`,
          estimatedCost: `#${party}DQExperts_details_0_estimatedCost`,
        },
        elements: {
          firstName: `#${party}DQExperts_details_0_firstName`,
          // lastName: `#${party}DQExperts_details_0_surname`,
          lastName: `#${party}DQExperts_details_0_lastName`,
          emailAddress: `#${party}DQExperts_details_0_emailAddress`,
          phoneNumber: `#${party}DQExperts_details_0_phoneNumber`,
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
      I.click(this.fields(party).expertRequired.options.yes);
    });

    await within(this.fields(party).expertReportsSent.id, () => {
      I.click(this.fields(party).expertReportsSent.options.yes);
    });

    await within(this.fields(party).jointExpertSuitable.id, () => {
      I.click(this.fields(party).jointExpertSuitable.options.yes);
    });

    await this.addExpert(party);

    await I.clickContinue();
  },

  async addExpert(party) {
    await I.addAnotherElementToCollection();
    I.waitForElement(this.fields(party).expertDetails.elements.firstName);
    I.fillField(this.fields(party).expertDetails.elements.firstName, 'John');
    I.fillField(this.fields(party).expertDetails.elements.lastName, 'Smith');
    I.fillField(this.fields(party).expertDetails.elements.emailAddress, 'johnsmith@email.com');
    I.fillField(this.fields(party).expertDetails.elements.phoneNumber, '07000111000');
    I.fillField(this.fields(party).expertDetails.elements.fieldOfExpertise, 'Science');
    I.fillField(this.fields(party).expertDetails.elements.whyRequired, 'Reason why required');
    I.fillField(this.fields(party).expertDetails.elements.estimatedCost, '100');
  },
};
