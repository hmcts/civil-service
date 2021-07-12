const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      disclosureFormFiledAndServed: {
        id: `#${party}DQDisclosureReport_disclosureFormFiledAndServed`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      disclosureProposalAgreed: {
        id: `#${party}DQDisclosureReport_disclosureProposalAgreed`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      draftOrderNumber: `#${party}DQDisclosureReport_draftOrderNumber`,
    };
  },

  async enterDisclosureReport(party) {
    I.waitForElement(this.fields(party).disclosureFormFiledAndServed.id);
    await I.runAccessibilityTest();
    await within (this.fields(party).disclosureFormFiledAndServed.id, () => {
      I.click(this.fields(party).disclosureFormFiledAndServed.options.yes);
    });

    await within (this.fields(party).disclosureProposalAgreed.id, () => {
      I.click(this.fields(party).disclosureProposalAgreed.options.yes);
    });

    I.fillField(this.fields(party).draftOrderNumber, '123456');

    await I.clickContinue();
  }
};
