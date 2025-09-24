const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      disclosureFormFiledAndServed: {
        id: `#${party}DQDisclosureReport_disclosureFormFiledAndServed`,
        options: {
          yes: `#${party}DQDisclosureReport_disclosureFormFiledAndServed_Yes`,
          no: `#${party}DQDisclosureReport_disclosureFormFiledAndServed_No`
        }
      },
      disclosureProposalAgreed: {
        id: `#${party}DQDisclosureReport_disclosureProposalAgreed`,
        options: {
          yes: `#${party}DQDisclosureReport_disclosureProposalAgreed_Yes`,
          no: `#${party}DQDisclosureReport_disclosureProposalAgreed_No`
        }
      },
      draftOrderNumber: `#${party}DQDisclosureReport_draftOrderNumber`,
    };
  },

  async enterDisclosureReport(party) {
    I.waitForElement(this.fields(party).disclosureFormFiledAndServed.id);
    await I.runAccessibilityTest();
    I.click(this.fields(party).disclosureFormFiledAndServed.options.yes);
    I.click(this.fields(party).disclosureProposalAgreed.options.yes);
    I.fillField(this.fields(party).draftOrderNumber, '123456');

    await I.clickContinue();
  }
};
