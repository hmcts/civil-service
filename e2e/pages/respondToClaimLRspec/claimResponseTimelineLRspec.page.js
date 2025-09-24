const { I } = inject();

module.exports = {
  fields: {
    claimTimelineOption: {
      id: '#specClaimResponseTimelineList',
      id2: '#specClaimResponseTimelineList2',
    },
  },

  async addManually(secondDef) {

      if(secondDef){
          I.waitForElement(this.fields.claimTimelineOption.id2);
          await I.runAccessibilityTest();
          await within(this.fields.claimTimelineOption.id2, () => {
          I.click('Add manually');
        });
      }else{
         I.waitForElement(this.fields.claimTimelineOption.id);
         await I.runAccessibilityTest();
         await within(this.fields.claimTimelineOption.id, () => {
         I.click('Add manually');
      });

      }
    await I.clickContinue();
  },
};
