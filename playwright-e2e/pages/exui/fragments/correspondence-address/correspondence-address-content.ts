import StringHelper from '../../../../helpers/string-helper';
import { Party } from '../../../../models/partys';

export const subheadings = {
  correspondenceAddress: 'Enter the correspondence address of the organisation',
};

export const radioButtons = {
  addressRequired: {
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressRequired_No`,
    },
  },
};

export const inputs = {
  postCodeLookup: {
    label: 'Enter a UK postcode',
    selector: (claimantDefendantParty: Party) => {
      return (
        `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails_` +
        `spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails_postcodeInput`
      );
    },
  },
  addressLine1: {
    label: 'Building and Street',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailAddressLine1`,
  },
  addressLine2: {
    label: 'address Line 2 (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailAddressLine2`,
  },
  addressLine3: {
    label: 'address Line 3 (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailAddressLine3`,
  },
  postTown: {
    label: 'Town or City (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailPostTown`,
  },
  county: {
    label: 'County (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailCounty`,
  },
  country: {
    label: 'Country (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailCountry`,
  },
  postCode: {
    label: 'Postcode/Zipcode',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails__detailPostCode`,
  },
};

export const dropdowns = {
  addressList: {
    label: 'Select an address',
    selector: (claimantDefendantParty: Party) =>
      `#spec${StringHelper.capitalise(claimantDefendantParty.oldPartyType)}${claimantDefendantParty.number === 1 ? '' : claimantDefendantParty.number}CorrespondenceAddressdetails_${claimantDefendantParty.oldKey}ServiceAddress_addressList`,
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
