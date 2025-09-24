const {I} = inject();

module.exports = {
  fields: {
    courtOrderType: {
      id: '#respondent1CourtOrderPayment_payingDetailsRequired_radio',
      options: {
        yes: '#respondent1CourtOrderPayment_payingDetailsRequired_Yes',
        no: '#respondent1CourtOrderPayment_payingDetailsRequired_No'
      },
    },
  },

  async selectRespondentCourtOrderType() {
    await within(this.fields.courtOrderType.id, () => {
    I.click(this.fields.courtOrderType.options.no);
    });
    await I.clickContinue();
  }
};
