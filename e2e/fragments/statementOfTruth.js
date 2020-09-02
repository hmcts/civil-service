const {I} = inject();

module.exports = {

  fields: {
    claim: {
      name: 'input[id$="applicantSolicitor1ClaimStatementOfTruth_name"',
      role: 'input[id$="applicantSolicitor1ClaimStatementOfTruth_role"',
    },
    service: {
      name: 'input[id$="applicant1ServiceStatementOfTruthToRespondentSolicitor1_name"',
      role: 'input[id$="applicant1ServiceStatementOfTruthToRespondentSolicitor1_role"',
    },
  },

  async enterNameAndRole(type = '', name = 'John Smith', role = 'Solicitor') {
    I.waitForElement(this.fields[type].name);
    I.fillField(this.fields[type].name, name);
    I.fillField(this.fields[type].role, role);
    await I.clickContinue();
  }
};
