const {I} = inject();
const personalInjuryTypePage = require('./personalInjuryType.page');
const declarationAndInjunctionPage = require('./declarationAndInjunction.page');
const humanRightsActPage = require('./humanRightsAct.page');

module.exports = {

  fields: {
    claimType: {
      id: '#claimType',
      options: {
        personalInjury: 'Personal injury',
        housingDisrepair: 'Housing disrepair',
        damagesAndOtherRemedy: 'Damages and an ‘other’ remedy e.g. Payment Protection Insurance (PPI), Motor finance'
      }
    },
  },

  async selectClaimType(claimType) {
    I.waitForText(this.fields.claimType.options.personalInjury, 60);
    if(claimType === 'Personal injury'){
      I.click(this.fields.claimType.options.personalInjury);
      await I.clickContinue();
      await personalInjuryTypePage.selectPersonalInjuryType();
    }else if(claimType === 'Housing disrepair' ){
      I.click(this.fields.claimType.options.housingDisrepair);
      await I.clickContinue();
      await declarationAndInjunctionPage.claimDeclaration('yes');
      await humanRightsActPage.humanRightsAct('yes');
    }else if(claimType === 'Damages and other remedy' ){
      I.click(this.fields.claimType.options.damagesAndOtherRemedy);
      await I.clickContinue();
      await declarationAndInjunctionPage.claimDeclaration('no');
      await humanRightsActPage.humanRightsAct('no');
    }
  }
};

