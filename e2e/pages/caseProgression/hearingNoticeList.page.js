const {I} = inject();

module.exports = {
  fields: {
    hearingNoticeType: {
      id: '#hearingNoticeList',
      options: {
        smallClaims: '#hearingNoticeList-SMALL_CLAIMS',
        fastTrack: '#hearingNoticeList-FAST_TRACK_TRIAL',
        others: '#hearingNoticeList-OTHER',
      }
    }
  },

  async hearingType(trackType) {
     await I.runAccessibilityTest();
     await I.waitForElement(this.fields.hearingNoticeType.id);
     if(trackType === 'smallClaims'){
        await I.click(this.fields.hearingNoticeType.options.smallClaims);
      } else if(trackType === 'fastTrack'){
        await I.click(this.fields.hearingNoticeType.options.fastTrack);
      } else {
        await I.click(this.fields.hearingNoticeType.options.others);
      }

      await I.clickContinue();
  }

};
