const {I} = inject();

module.exports = {

  fields: {
    claim: {
      name: 'input[id$="applicantSolicitor1ClaimStatementOfTruth_name"',
      role: 'input[id$="applicantSolicitor1ClaimStatementOfTruth_role"',
    },
    respondent1DQ: {
      name: 'input[id$="respondent1DQStatementOfTruth_name"',
      role: 'input[id$="respondent1DQStatementOfTruth_role"',
    },
    applicant1DQ: {
      name: 'input[id$="applicant1DQStatementOfTruth_name"',
      role: 'input[id$="applicant1DQStatementOfTruth_role"',
    }
  },

  async enterNameAndRole(type = '', name = 'John Smith', role = 'Solicitor') {
    I.waitForElement(this.fields[type].name);
    I.fillField(this.fields[type].name, name);
    I.fillField(this.fields[type].role, role);
    await I.clickContinue();
  }
};
