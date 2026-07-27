const { I } = inject();

module.exports = {
  fields: {
    allocateTrack: {
      id: '#finalOrderAllocateToTrack_radio',
      options: {
        yes: '#finalOrderAllocateToTrack_Yes',
        no: '#finalOrderAllocateToTrack_No'
      }
    },
    trackType: {
      id: '#finalOrderTrackAllocation',
      options: {
        intermediateTrack: '#finalOrderTrackAllocation-INTERMEDIATE_CLAIM',
        multiTrack: '#finalOrderTrackAllocation-MULTI_CLAIM'
      }
    },
    labels: {
      allocateQuestion: 'What track are you allocating or re-allocating the claim to?'
    }
  },

  async allocationTrack(isTrack, trackType) {
    const TRACK_TYPES = {
      INTERMEDIATE: 'Intermediate Track',
      MULTI: 'Multi Track'
    };

    try {
      if (isTrack === 'Yes') {
        I.click(this.fields.allocateTrack.options.yes);
        I.waitForText(this.fields.labels.allocateQuestion);
        if (trackType === TRACK_TYPES.INTERMEDIATE) {
          I.click(this.fields.trackType.options.intermediateTrack);
        } else if (trackType === TRACK_TYPES.MULTI) {
          I.click(this.fields.trackType.options.multiTrack);
        } else {
          throw new Error(`Invalid track type: ${trackType}`);
        }
      } else {
        I.click(this.fields.allocateTrack.options.no);
      }

      await I.clickContinue();
    } catch (error) {
      console.error(`Error during track allocation: ${error.message}`);
      throw error;
    }
  }
};
