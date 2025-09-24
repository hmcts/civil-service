const { I } = inject();

module.exports = {
  fields: {
    complexityBand: {
      assignComplexity: {
        yes: '#finalOrderIntermediateTrackComplexityBand_assignComplexityBand_Yes',
        no: '#finalOrderIntermediateTrackComplexityBand_assignComplexityBand_No',
      },
      bands: {
        band1: '#finalOrderIntermediateTrackComplexityBand_band-BAND_1',
        band2: '#finalOrderIntermediateTrackComplexityBand_band-BAND_2',
        band3: '#finalOrderIntermediateTrackComplexityBand_band-BAND_3',
        band4: '#finalOrderIntermediateTrackComplexityBand_band-BAND_4',
      },
      reasons: '#finalOrderIntermediateTrackComplexityBand_reasons',
    }
  },

  async selectComplexityBand(assignComplexity, bandOption, reason) {
    if (assignComplexity === 'Yes') {
      I.click(this.fields.complexityBand.assignComplexity.yes);
      if (bandOption === 'Band 1') {
        I.click(this.fields.complexityBand.bands.band1);
      } else if (bandOption === 'Band 2') {
        I.click(this.fields.complexityBand.bands.band2);
      } else if (bandOption === 'Band 3') {
        I.click(this.fields.complexityBand.bands.band3);
      } else if (bandOption === 'Band 4') {
        I.click(this.fields.complexityBand.bands.band4);
      } else {
        throw new Error(`Invalid band option: ${bandOption}`);
      }
      if (reason) {
        I.fillField(this.fields.complexityBand.reasons, reason);
      }
    } else if (assignComplexity === 'No') {
      I.click(this.fields.complexityBand.assignComplexity.no);
    }
    await I.clickContinue();
  }
};
