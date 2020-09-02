const {I} = inject();

module.exports = {

  fields: {
    serviceLocation: {
      id: '#serviceLocationToRespondentSolicitor1_serviceLocationToRespondentSolicitor1',
      options: {
        usualResidence: 'Usual residence',
        placeOfBusiness: 'Place of business',
        other: 'Other'
      }
    }
  },

  async selectUsualResidence() {
    I.waitForElement(this.fields.serviceLocation.id);
    await within(this.fields.serviceLocation.id, () => {
      I.click(this.fields.serviceLocation.options.usualResidence);
    });

    await I.clickContinue();
  }
};

