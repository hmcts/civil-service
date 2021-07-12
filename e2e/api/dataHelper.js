const uuid = require('uuid');
const config = require('../config.js');
const address = require('../fixtures/address');

const getDateTimeISOString = days => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString();
};

module.exports = {
  date: (days = 0) => {
    return getDateTimeISOString(days).slice(0, 10);
  },

  dateTime: (days = 0) => {
    return getDateTimeISOString(days);
  },

  document: filename => {
    return {
      document_url: `${config.url.dmStore}/documents/fakeUrl`,
      document_filename: filename,
      document_binary_url: `${config.url.dmStore}/documents/fakeUrl/binary`
    };
  },

  element: object => {
    return {
      id: uuid.v1(),
      value: object
    };
  },

  listElement: string => {
    return {
      code: uuid.v1(),
      label: string
    };
  },

  buildAddress: postFixLineOne => {
    return {
      AddressLine1: `${address.buildingAndStreet.lineOne + ' - ' + postFixLineOne}`,
      AddressLine2: address.buildingAndStreet.lineTwo,
      AddressLine3: address.buildingAndStreet.lineThree,
      PostTown: address.town,
      County: address.county,
      Country: address.country,
      PostCode: address.postcode
    };
  }
};
