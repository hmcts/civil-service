const {I} = inject();

module.exports = {
  fields: {
    debtsType: {
      id: '#specDefendant1Debts_hasLoanCardDebt_radio',
      options: {
        yes: '#specDefendant1Debts_hasLoanCardDebt_Yes',
        no: '#specDefendant1Debts_hasLoanCardDebt_No'
      },
    },
  },

  async selectDebtsDetails() {
    await within(this.fields.debtsType.id, () => {
    I.click(this.fields.debtsType.options.no);
    });
    await I.clickContinue();
  }
};
