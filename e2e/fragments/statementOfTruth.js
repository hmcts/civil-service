const {I} = inject();

module.exports = {

  fields: function (id) {
    return {
      name: `input[id$="${id}StatementOfTruth_name"]`,
      role: `input[id$="${id}StatementOfTruth_role"]`,
    };
  },

  async enterNameAndRole(fieldID = '', name = 'John Smith', role = 'Solicitor') {
    I.fillField(this.fields(fieldID).name, name);
    I.fillField(this.fields(fieldID).role, role);
    await I.clickContinue();
  }
};
