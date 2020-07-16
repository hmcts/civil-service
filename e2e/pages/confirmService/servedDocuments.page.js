const {I} = inject();

module.exports = {

  fields: {
    servedDocuments: {
      id: '#servedDocuments',
      options: {
        claimForm: 'Claim form',
        particularsOfClaim: 'Particulars of claim',
        responsePack: 'Response pack',
        medicalReports: 'Medical reports',
        scheduleOfLoss: 'Schedule of loss',
        certificateOfSuitability: 'Certificate of suitability',
        other: 'Other'
      }
    },
    other: '#servedDocumentsOther'
  },

  async enterServedDocuments() {
    await within(this.fields.servedDocuments.id, () => {
      I.click(this.fields.servedDocuments.options.responsePack);
      I.click(this.fields.servedDocuments.options.medicalReports);
      I.click(this.fields.servedDocuments.options.scheduleOfLoss);
      I.click(this.fields.servedDocuments.options.certificateOfSuitability);
      I.click(this.fields.servedDocuments.options.other);
    });

    I.fillField(this.fields.other, 'Other documents uploaded');
    await I.clickContinue();
  }
};

