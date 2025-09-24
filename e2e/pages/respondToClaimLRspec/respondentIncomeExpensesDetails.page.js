const {I} = inject();

module.exports = {
  fields: {
    selectIncomeExpensesType: {
      id: '#respondent1DQCarerAllowanceCreditFullAdmission_radio',
      options: {
        yes: '#respondent1DQCarerAllowanceCreditFullAdmission_Yes',
        no: '#respondent1DQCarerAllowanceCreditFullAdmission_No'
      }
    }
  },

  async selectIncomeExpenses() {
    await within(this.fields.selectIncomeExpensesType.id, () => {
    I.click(this.fields.selectIncomeExpensesType.options.no);
    });
    await I.clickContinue();
  }
};
