const { I } = inject();

module.exports = {

  fields: {
    courtLocation: '#courtLocation_preferredCourt'
  },

  async enterCourt() {
      I.fillField(this.fields.courtLocation, 'London High Court');
      await I.clickContinue();
  }
};

