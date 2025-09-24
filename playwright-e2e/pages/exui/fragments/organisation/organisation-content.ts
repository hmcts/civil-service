import { Party } from '../../../../models/partys';

export const subheadings = {
  search: 'Search for an organisation',
  organisations: 'Organisation name and address',
};

export const inputs = {
  organisationReference: {
    label: 'Reference (Optional)',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}OrganisationPolicy_OrgPolicyReference`,
  },
  search: {
    label:
      'You can only search for organisations already registered with MyHMCTS. For example, you can search by organisation name or address.',
    selector: '#search-org-text',
  },
};

export const links = {
  selectOrganisation: {
    title: 'Select',
    selector: (organisationName: string) =>
      `a[title='Select the organisation ${organisationName}']`,
  },
  cannotFindOrganisation: {
    title: "Can't find the organisation you are looking for?",
    selector: '#content-why-can-not-find-organisation',
  },
};
