const { I } = inject();

module.exports = {
  fields: function (party) {
    return {
      fields: {
        postcodeLookup: 'input[id$="postcodeInput"]',
        addressList: 'select[id$="addressList"]',
        buildingAndStreet: {
          lineOne: `input[id$="spec${party}CorrespondenceAddressdetails__detailAddressLine1"]`,
          lineTwo: `input[id$="spec${party}CorrespondenceAddressdetails__detailAddressLine2"]`,
          lineThree: `input[id$="spec${party}CorrespondenceAddressdetails__detailAddressLine3"]`,
        },
        town: `input[id$="spec${party}CorrespondenceAddressdetails__detailPostTown"]`,
        county: `input[id$="spec${party}CorrespondenceAddressdetails__detailPostTown"]`,
        country: `input[id$="spec${party}CorrespondenceAddressdetails__detailCountry"]`,
        postcode: `input[id$="spec${party}CorrespondenceAddressdetails__detailPostCode"]`,
      },
      findAddressButton: 'Find address',
      cantEnterPostcodeLink: locate('a').withText('I can\'t enter a UK postcode'),
    };
  },

  enterAddressManually(party, address) {
    I.click(this.fields(party).cantEnterPostcodeLink);
    I.fillField(this.fields(party).fields.buildingAndStreet.lineOne, address.buildingAndStreet.lineOne);
    I.fillField(this.fields(party).fields.buildingAndStreet.lineTwo, address.buildingAndStreet.lineTwo);
    I.fillField(this.fields(party).fields.buildingAndStreet.lineThree, address.buildingAndStreet.lineThree);
    I.fillField(this.fields(party).fields.town, address.town);
    I.fillField(this.fields(party).fields.county, address.county);
    I.fillField(this.fields(party).fields.country, address.country);
    I.fillField(this.fields(party).fields.postcode, address.postcode);
  },
};
