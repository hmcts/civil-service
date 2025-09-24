import { Party } from '../../../../models/partys';

export const subheadings = {
  litigationFriendAddress: "Litigation friend's address",
  uploadcertificate: 'Upload the certificate of suitability',
  litigationDetails: (litigationParty: Party) =>
    `Defendant ${litigationParty.number === 1 ? '' : litigationParty.number} litigation details`,
};

export const inputs = {
  litigationFriendDetails: {
    firstName: {
      label: 'First name',
      selector: (litigationFriendParty: Party) => `#${litigationFriendParty.oldKey}_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (litigationFriendParty: Party) => `#${litigationFriendParty.oldKey}_lastName`,
    },
    email: {
      label: 'Email address (Optional)',
      selector: (litigationFriendParty: Party) => `#${litigationFriendParty.oldKey}_emailAddress`,
    },
    phoneNumber: {
      label: 'Phone number (Optional)',
      selector: (litigationFriendParty: Party) => `#${litigationFriendParty.oldKey}_phoneNumber`,
    },
  },
  address: {
    postCodeInput: {
      label: 'Enter a UK postcode',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress_primaryAddress_postcodeInput`,
    },
    addressLine1: {
      label: 'Building and Street',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailAddressLine1`,
    },
    addressLine2: {
      label: 'Address Line 2',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailAddressLine2`,
    },
    addressLine3: {
      label: 'Address Line 3',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailAddressLine3`,
    },
    postTown: {
      label: 'Town or City',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailPostTown`,
    },
    county: {
      label: 'County',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailCounty`,
    },
    country: {
      label: 'Country',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailCountry`,
    },
    postCode: {
      label: 'Postcode/Zipcode',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_primaryAddress__detailPostCode`,
    },
  },
  certificateOfSuitability: {
    uploadDoc: {
      label: 'Document',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_certificateOfSuitability_0_document`,
    },
  },
};

export const radioButtons = {
  address: {
    yes: {
      label: 'Yes',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_hasSameAddressAsLitigant_Yes`,
    },
    no: {
      label: 'Yes',
      selector: (litigationFriendParty: Party) =>
        `#${litigationFriendParty.oldKey}_hasSameAddressAsLitigant_No`,
    },
  },
};

export const buttons = {
  addNewCertificate: {
    title: 'Add new',
    selector: (litigationFriendParty: Party) =>
      `div[id='${litigationFriendParty.oldKey}_certificateOfSuitability'] button[type='button']`,
  },
  findaddress: {
    title: 'Find address',
  },
};

export const links = {
  cannotFindAddress: {
    title: " I can't enter a UK postcode ",
  },
};
