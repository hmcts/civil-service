const {I} = inject();

module.exports = {

  fields: {
    orgPolicyReference: '#applicant1OrganisationPolicy_OrgPolicyReference',
    searchText: '#search-org-text',
    organisationSelected: '#organisation-selected-table'
  },

  async enterOrganisationDetails() {
    I.waitForElement(this.fields.orgPolicyReference);
    await I.runAccessibilityTest();
    I.fillField(this.fields.orgPolicyReference, 'Claimant policy reference');
    I.waitForElement(this.fields.searchText);
    if (!(await I.hasSelector(this.fields.organisationSelected))) {
      I.fillField(this.fields.searchText, 'Civil');
      I.click('a[title="Select the organisation Civil - Organisation 1"]');
    }
    await I.clickContinue();
  }
};

