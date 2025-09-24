import { Party } from '../../../../models/partys';

export const inputs = {
  postCodeInput: {
    label: 'Enter a UK postcode',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress_primaryAddress_postcodeInput`,
  },
  addressLine1: {
    label: 'Building and Street',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailAddressLine1`,
  },
  addressLine2: {
    label: 'Address Line 2 (Optional)',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailAddressLine2`,
  },
  addressLine3: {
    label: 'Address Line 3 (Optional)',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailAddressLine3`,
  },
  postTown: {
    label: 'Town or City (Optional)',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailPostTown`,
  },
  county: {
    label: 'County (Optional)',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailCounty`,
  },
  country: {
    label: 'Country (Optional)',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailCountry`,
  },
  postCode: {
    label: 'Postcode/Zipcode',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress__detailPostCode`,
  },
};

export const dropdowns = {
  addressList: {
    label: 'Select an address',
    selector: (party: Party) => `#${party.oldKey}_primaryAddress_primaryAddress_addressList`,
    options: [],
  },
};

export const buttons = {
  findaddress: {
    title: 'Find address',
  },
};

export const links = {
  cannotFindAddress: {
    title: " I can't enter a UK postcode ",
  },
};
