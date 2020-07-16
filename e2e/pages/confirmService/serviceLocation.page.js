const {I} = inject();

module.exports = {

  fields: {
    serviceLocation: {
      id: '#serviceLocation_serviceLocation',
      options: {
        usualResidence: 'Usual residence',
        placeOfBusiness: 'Place of business',
        other: 'Other'
      }
    }
  },

  async selectUsualResidence() {
    await within(this.fields.serviceLocation.id, () => {
      I.click(this.fields.serviceLocation.options.usualResidence);
    });

    await I.clickContinue();
  }
};

