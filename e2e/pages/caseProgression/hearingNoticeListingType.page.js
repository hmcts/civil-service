const {I} = inject();

module.exports = {
  fields: {
    listingOrRelisting: {
      id: '#listingOrRelisting',
      options: {
        listing: '#listingOrRelisting-LISTING',
        reListing: '#listingOrRelisting-RELISTING'
      }
    }
  },

  async listingOrRelistingSelect(selectType) {
      await I.runAccessibilityTest();
        if(selectType === 'Listing'){
            I.click(this.fields.listingOrRelisting.options.listing);
          } else{
            I.click(this.fields.listingOrRelisting.options.reListing);
        }
      await I.clickContinue();
    }
};
