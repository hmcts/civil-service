import { Party } from '../../../../../../models/partys';

export const subheadings = { mediationContact: 'Mediation contact information' };

export const paragraphs = {
  description1:
    'Please provide the contact details of the individual who will conduct the mediation appointment.',
  description2: 'This should be a party to the claim or their legal representative.',
};

export const inputs = {
  mediationFriendDetails: {
    firstName: {
      label: 'First name',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationContactInfo_firstName`,
    },
    lastName: {
      label: 'Last name',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationContactInfo_lastName`,
    },
    emailAddress: {
      label: 'Email address',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationContactInfo_emailAddress`,
    },
    telephoneNumber: {
      label: 'Telephone number',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationContactInfo_telephoneNumber`,
    },
  },
};
