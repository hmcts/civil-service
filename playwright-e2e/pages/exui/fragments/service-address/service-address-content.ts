import PartyType from '../../../../enums/party-types';
import StringHelper from '../../../../helpers/string-helper';
import { Party } from '../../../../models/partys';

export const subheadings = {
  correspondenceAddress: 'Enter the correspondence address of the organisation',
};

export const radioButtons = {
  addressRequired: {
    label: (claimantDefendantParty: Party) =>
      `Postal correspondence for the ${StringHelper.capitalise(claimantDefendantParty.partyType === PartyType.CLAIMANT ? claimantDefendantParty.partyType : claimantDefendantParty.oldPartyType)}’s legal representative will be sent to the address registered with MyHMCTS. ` +
      'You can, if you wish, change the address to which postal correspondence is sent (eg if you work out of a different office from the address registered with MyHMCTS). ' +
      'Do you wish to enter a different address?',
    hintText: (claimantDefendantParty: Party) =>
      `This is the address to which postal correspondence for the ${StringHelper.capitalise(claimantDefendantParty.partyType === PartyType.CLAIMANT ? claimantDefendantParty.partyType : claimantDefendantParty.oldPartyType)}’s legal representative will be sent.`,
    yes: {
      label: 'Yes',
      selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddressRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddressRequired_No`,
    },
  },
};

export const inputs = {
  postCodeLookup: {
    label: 'Enter a UK postcode',
    selector: (solicitorParty: Party) => {
      return `#${solicitorParty.oldKey}ServiceAddress_${solicitorParty.oldKey}ServiceAddress_postcodeInput`;
    },
  },
  addressLine1: {
    label: 'Building and Street',
    selector: (solicitorParty: Party) =>
      `#${solicitorParty.oldKey}ServiceAddress__detailAddressLine1`,
  },
  addressLine2: {
    label: 'address Line 2 (Optional)',
    selector: (solicitorParty: Party) =>
      `#${solicitorParty.oldKey}ServiceAddress__detailAddressLine2`,
  },
  addressLine3: {
    label: 'address Line 3 (Optional)',
    selector: (solicitorParty: Party) =>
      `#${solicitorParty.oldKey}ServiceAddress__detailAddressLine3`,
  },
  postTown: {
    label: 'Town or City (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddress__detailPostTown`,
  },
  county: {
    label: 'County (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddress__detailCounty`,
  },
  country: {
    label: 'Country (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddress__detailCountry`,
  },
  postCode: {
    label: 'Postcode/Zipcode',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}ServiceAddress__detailPostCode`,
  },
};

export const dropdowns = {
  addressList: {
    label: 'Select an address',
    selector: (solicitorParty: Party) =>
      `#${solicitorParty.oldKey}ServiceAddress_${solicitorParty.oldKey}ServiceAddress_addressList`,
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
