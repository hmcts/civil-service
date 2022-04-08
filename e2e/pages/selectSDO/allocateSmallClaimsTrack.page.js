const {I} = inject();

module.exports = {
  fields: {
    drawDirectionsOrderSmallClaims: {
      id: '#drawDirectionsOrderSmallClaims_radio',
      options: {
        yes: '#drawDirectionsOrderSmallClaims_Yes',
        no: '#drawDirectionsOrderSmallClaims_No'
      }
    }
  },

  async decideSmallClaimsTrack(smallClaims) {
    await I.runAccessibilityTest();
    await within(this.fields.drawDirectionsOrderSmallClaims.id, () => {
      const { yes, no } = this.fields.drawDirectionsOrderSmallClaims.options;
      I.click(smallClaims ? yes : no);
    });
    await I.clickContinue();
  }
};
