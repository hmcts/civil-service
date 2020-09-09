const {I} = inject();

module.exports = {

  fields: {
    personalInjuryType: {
      id: '#personalInjuryType',
      options: {
        roadAccident: 'Road accident'
      }
    },
  },

  async selectPersonalInjuryType() {
    await within(this.fields.personalInjuryType.id, () => {
      I.click(this.fields.personalInjuryType.options.roadAccident);
    });
    await I.clickContinue();
  }
};

