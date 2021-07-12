const {I} = inject();

module.exports = {

  fields: {
    postcodeLookup: 'input[id$="postcodeInput"]',
    addressList: 'select[id$="addressList"]',
    buildingAndStreet: {
      lineOne: 'input[id$="AddressLine1"]',
      lineTwo: 'input[id$="AddressLine2"]',
      lineThree: 'input[id$="AddressLine3"]',
    },
    town: 'input[id$="PostTown"]',
    county: 'input[id$="County"]',
    country: 'input[id$="Country"]',
    postcode: 'input[id$="PostCode"]',
  },
  findAddressButton: 'Find address',
  cantEnterPostcodeLink: locate('a').withText('I can\'t enter a UK postcode'),

  enterAddressManually(address) {
    I.click(this.cantEnterPostcodeLink);
    I.fillField(this.fields.buildingAndStreet.lineOne, address.buildingAndStreet.lineOne);
    I.fillField(this.fields.buildingAndStreet.lineTwo, address.buildingAndStreet.lineTwo);
    I.fillField(this.fields.buildingAndStreet.lineThree, address.buildingAndStreet.lineThree);
    I.fillField(this.fields.town, address.town);
    I.fillField(this.fields.county, address.county);
    I.fillField(this.fields.country, address.country);
    I.fillField(this.fields.postcode, address.postcode);
  }
};
