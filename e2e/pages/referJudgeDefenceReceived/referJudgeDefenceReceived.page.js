const {I} = inject();

module.exports = {
  fields: {
    confirm: {
      id: '#confirmReferToJudgeDefenceReceived-CONFIRM',
    },
  },

  async selectConfirm() {
    I.waitForElement(this.fields.confirm.id);
    await I.runAccessibilityTest();
    I.click(this.fields.confirm.id);
  },

};