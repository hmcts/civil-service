const {I} = inject();

module.exports = {

  fields: {
    name: 'input[id$="statementOfTruth_name"]',
    role: 'input[id$="statementOfTruth_role"]',
  },

  enterNameAndRole(name = 'John Smith', role = 'Solicitor') {
    I.fillField(this.fields.name, name);
    I.fillField(this.fields.role, role);
    I.clickContinue();
  }
};
