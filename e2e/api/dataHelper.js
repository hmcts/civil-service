const fetch = require('node-fetch');
const uuid = require('uuid');
const config = require('../config.js');
const address = require('../fixtures/address');
const getDateTimeISOString = days => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date.toISOString();
};

const getDate = days => {
  const date = new Date();
  date.setDate(date.getDate() + days);
  return date;
};


module.exports = {
  date: (days = 0) => {
    return getDateTimeISOString(days).slice(0, 10);
  },

  dateNoWeekends: async function dateNoWeekends(days = 0) {
    const date = getDate(days);
    let date_String = date.toISOString().slice(0, 10);
    let isDateABankHoliday = false;
    if (date.getDay() !== 6 && date.getDay() !== 0) {
        try {
          const rawBankHolidays = await fetch('https://www.gov.uk/bank-holidays.json');
          const ukbankholidays = await rawBankHolidays.json();
          isDateABankHoliday = JSON.stringify(ukbankholidays['england-and-wales'].events).includes(date_String);
          if (!isDateABankHoliday) {
            return date_String;
          } else {
            return await dateNoWeekends(days - 1);
          }
        } catch (err) {
          console.warn('Error while fetching UK Bank Holidays...', err);
        }
    } else {
      return await dateNoWeekends(days - 1);
    }
  },

  dateNoWeekendsBankHolidayNextDay: async function dateNoWeekendsBankHolidayNextDay(days = 0) {
    const date = getDate(days);
    let date_String = date.toISOString().slice(0, 10);
    let isDateABankHoliday = false;
    if (date.getDay() !== 6 && date.getDay() !== 0) {
      try {
        const rawBankHolidays = await fetch('https://www.gov.uk/bank-holidays.json');
        const ukbankholidays = await rawBankHolidays.json();
        isDateABankHoliday = JSON.stringify(ukbankholidays['england-and-wales'].events).includes(date_String);
        if (!isDateABankHoliday) {
          return date_String;
        } else {
          return await dateNoWeekendsBankHolidayNextDay(days + 1);
        }
      } catch (err) {
        console.warn('Error while fetching UK Bank Holidays...', err);
      }
    } else {
      return await dateNoWeekendsBankHolidayNextDay(days + 1);
    }
  },

  dateTime: (days = 0) => {
    return getDateTimeISOString(days);
  },
  incrementDate: (date = new Date(), dayIncrement, monthIncrement, yearIncrement) => {
    const newDate = new Date(date);
    if(dayIncrement) {
      newDate.setDate(newDate.getDate() + dayIncrement);
    }
    if(monthIncrement) {
      newDate.setMonth(newDate.getMonth() + monthIncrement);
    }
    if(yearIncrement) {
      newDate.setYear(newDate.getFullYear() + yearIncrement);
    }
    return newDate;
  },
  appendTime: (date = new Date(), hours, minutes) => {
    const newDate = new Date(date);
    newDate.setHours(hours ? hours : date.getHours());
    newDate.setMinutes(minutes ? minutes : date.getMinutes());
    return newDate;
  },
  document: filename => {
    const documentId = uuid.v1();
    return {
      document_url: `${config.url.dmStore}/documents/${documentId}`,
      document_filename: filename,
      document_binary_url: `${config.url.dmStore}/documents/${documentId}/binary`
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

  listElementWithCode: (code, string) => {
    return {
      code: code,
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
  },
  buildBulkClaimAddress: () => {
    return {
      addressLine1: address.buildingAndStreet.lineOne,
      addressLine2: address.buildingAndStreet.lineTwo,
      addressLine3: address.town,
      addressLine4: address.country,
      postcode: address.postcode
    };
  }
};
