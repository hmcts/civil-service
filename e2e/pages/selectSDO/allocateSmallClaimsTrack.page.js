const {I} = inject();

module.exports = {
  fields: {
    drawDirectionsOrderSmallClaims: {
      id: '#drawDirectionsOrderSmallClaims_radio',
      options: {
        yes: '#drawDirectionsOrderSmallClaims_Yes',
        no: '#drawDirectionsOrderSmallClaims_No'
      }
    },
    drawDirectionsOrderSmallClaimsAdditionalDirections: {
      id: '#drawDirectionsOrderSmallClaimsAdditionalDirections',
      creditHire: '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimCreditHire',
      roadTrafficAccident: '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimRoadTrafficAccident'
    }
  },

  async decideSmallClaimsTrack(smallClaims) {
    await I.runAccessibilityTest();
    await within(this.fields.drawDirectionsOrderSmallClaims.id, () => {
      const { yes, no } = this.fields.drawDirectionsOrderSmallClaims.options;
      I.click(smallClaims ? yes : no);
    });
    if (smallClaims) {
      I.waitForElement(this.fields.drawDirectionsOrderSmallClaimsAdditionalDirections.id);
      I.checkOption(this.fields.drawDirectionsOrderSmallClaimsAdditionalDirections.creditHire);
      I.checkOption(this.fields.drawDirectionsOrderSmallClaimsAdditionalDirections.roadTrafficAccident);
    }
    await I.clickContinue();
  }
};
