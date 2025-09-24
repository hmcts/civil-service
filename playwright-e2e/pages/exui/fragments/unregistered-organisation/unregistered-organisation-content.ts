import { Party } from '../../../../models/partys';

export const content = 'Enter organisation details manually. The claim will then continue offline.';

export const inputs = {
  organisationName: {
    label: 'Organisation name',
    selector: (solicitorParty: Party) =>
      `#${solicitorParty.oldKey}OrganisationDetails_organisationName`,
  },
  phoneNumber: {
    label: 'Phone number (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}OrganisationDetails_phoneNumber`,
  },
  email: {
    label: 'Email (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}OrganisationDetails_email`,
  },
  DX: {
    label: 'DX (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}OrganisationDetails_dx`,
  },
  fax: {
    label: 'Fax (Optional)',
    selector: (solicitorParty: Party) => `#${solicitorParty.oldKey}OrganisationDetails_fax`,
  },
};
