const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      reachedAgreement: {
        id: `#${party}DQDisclosureOfElectronicDocuments_reachedAgreement`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      agreementLikely: {
        id: `#${party}DQDisclosureOfElectronicDocuments_agreementLikely`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      reasonForNoAgreement: `#${party}DQDisclosureOfElectronicDocuments_reasonForNoAgreement`,
    };
  },

  async enterDisclosureOfElectronicDocuments(party) {
    I.waitForElement(this.fields(party).reachedAgreement.id);
    await within(this.fields(party).reachedAgreement.id, () => {
      I.click(this.fields(party).reachedAgreement.options.no);
    });

    await within(this.fields(party).agreementLikely.id, () => {
      I.click(this.fields(party).agreementLikely.options.no);
    });

    I.fillField(this.fields(party).reasonForNoAgreement, 'Reason for no agreement');

    await I.clickContinue();
  }
};
