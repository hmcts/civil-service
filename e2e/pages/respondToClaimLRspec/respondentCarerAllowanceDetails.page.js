const {I} = inject();

module.exports = {
  fields: {
    selectIncomeExpensesType: {
      id: '#respondent1DQCarerAllowanceCredit_radio',
      options: {
        yes: '#respondent1DQCarerAllowanceCredit_Yes',
        no: '#respondent1DQCarerAllowanceCredit_No'
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
