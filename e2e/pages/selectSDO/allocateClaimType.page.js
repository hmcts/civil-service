const {I} = inject();

module.exports = {
  fields: {
    claimsTrack: {
      id: '#claimsTrack',
      options: {
        smallClaimsTrack: '#claimsTrack-smallClaimsTrack',
        fastTrack: '#claimsTrack-fastTrack'
      }
    },
    smallClaimIds:{
      id: '#smallClaims',
      creditHire: '#smallClaims-smallClaimCreditHire',
      roadTrafficAccident: '#smallClaims-smallClaimRoadTrafficAccident'
    },
    fastClaims:{
      id: '#fastClaims',
      buildingDispute: '#fastClaims-fastClaimBuildingDispute',
      clinicalNegligence: '#fastClaims-fastClaimClinicalNegligence',
      creditHire: '#fastClaims-fastClaimCreditHire',
      employersLiability: '#fastClaims-fastClaimEmployersLiability',
      housingDisrepair: '#fastClaims-fastClaimHousingDisrepair',
      personalInjury: '#fastClaims-fastClaimPersonalInjury',
      roadTrafficAccident: '#fastClaims-fastClaimRoadTrafficAccident'
    }
  },

  async selectTrackType(trackType) {
    await I.runAccessibilityTest();
    if(trackType === 'smallClaims'){
      I.click(this.fields.claimsTrack.options.smallClaimsTrack);
      I.waitForElement(this.fields.smallClaimIds.id);
      I.checkOption(this.fields.smallClaimIds.creditHire);
    }
    else{
      I.click(this.fields.claimsTrack.options.fastTrack);
      I.waitForElement((this.fields.fastClaims.id));
      I.checkOption(this.fields.fastClaims.clinicalNegligence);
      I.checkOption(this.fields.fastClaims.roadTrafficAccident);
    }
    await I.clickContinue();
  }
};
