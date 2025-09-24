const {I} = inject();
const {checkFastTrackUpliftsEnabled} = require('./../../api/testingSupport');

module.exports = {

  fields: function (party) {
    return {
      isSubjectToFixedRecoverableCostRegime: {
        id: `#${party}DQFixedRecoverableCosts_isSubjectToFixedRecoverableCostRegime`,
        options: {
          yes: `#${party}DQFixedRecoverableCosts_isSubjectToFixedRecoverableCostRegime_Yes`,
          no: `#${party}DQFixedRecoverableCosts_isSubjectToFixedRecoverableCostRegime_No`
        }
      },
      intFixedRecoverableCostRegime: {
        id: `#${party}DQFixedRecoverableCostsIntermediate_isSubjectToFixedRecoverableCostRegime`,
        options: {
          yes: `#${party}DQFixedRecoverableCostsIntermediate_isSubjectToFixedRecoverableCostRegime_Yes`,
          no: `#${party}DQFixedRecoverableCostsIntermediate_isSubjectToFixedRecoverableCostRegime_No`
        }
      },
      isComplexityBandingAgreed: {
        id: `#${party}DQFixedRecoverableCostsIntermediate_complexityBandingAgreed`,
        options: {
          yes: `#${party}DQFixedRecoverableCostsIntermediate_complexityBandingAgreed_Yes`,
          no: `#${party}DQFixedRecoverableCostsIntermediate_complexityBandingAgreed_No`
        }
      },
      band: {
        id: `#${party}DQFixedRecoverableCosts_band`,
        options: {
          band1: 'BAND_1',
          band2: 'BAND_2',
          band3: 'BAND_3',
          band4: 'BAND_4'
        }
      },
      intBand: {
        id: `#${party}DQFixedRecoverableCostsIntermediate_band`,
        options: {
          band1: 'BAND_1',
          band2: 'BAND_2',
          band3: 'BAND_3',
          band4: 'BAND_4'
        }
      },
      complexityBandingAgreed: {
        id: `#${party}DQFixedRecoverableCosts_complexityBandingAgreed`,
        options: {
          yes: `#${party}DQFixedRecoverableCosts_complexityBandingAgreed_Yes`,
          no: `#${party}DQFixedRecoverableCosts_complexityBandingAgreed_No`
        }
      },
      reasons: `#${party}DQFixedRecoverableCosts_reasons`,
      intFRCReasons: `#${party}DQFixedRecoverableCostsIntermediate_reasons`,
    };
  },

  async fixedRecoverableCosts(party) {
    let fastTrackUpliftsEnabled = await checkFastTrackUpliftsEnabled();
    if (fastTrackUpliftsEnabled) {
      await within(this.fields(party).isSubjectToFixedRecoverableCostRegime.id, () => {
        I.runAccessibilityTest();
        I.click(`${this.fields(party).isSubjectToFixedRecoverableCostRegime.options.yes}`);
      });

      await within(this.fields(party).band.id, () => {
        I.click(`${this.fields(party).band.id}-${this.fields(party).band.options.band1}`);
      });

      await within(this.fields(party).complexityBandingAgreed.id, () => {
        I.click(this.fields(party).complexityBandingAgreed.options.yes);
      });

      I.fillField(this.fields(party).reasons, 'Some good reasons');

      await I.clickContinue();
    }
  },

  async fixedRecoverableCostsInt(party) {
      await within(this.fields(party).intFixedRecoverableCostRegime.id, () => {
        I.runAccessibilityTest();
        I.click(`${this.fields(party).intFixedRecoverableCostRegime.options.yes}`);
      });

    await within(this.fields(party).isComplexityBandingAgreed.id, () => {
      I.click(`${this.fields(party).isComplexityBandingAgreed.options.yes}`);
    });

      await within(this.fields(party).intBand.id, () => {
        I.click(`${this.fields(party).intBand.id}-${this.fields(party).band.options.band2}`);
      });
      I.fillField(this.fields(party).intFRCReasons, 'Some good reasons');
      await I.clickContinue();
  }
};
