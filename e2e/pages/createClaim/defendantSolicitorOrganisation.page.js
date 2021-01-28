const {I} = inject();

module.exports = {

  fields: {
    orgPolicyReference: '#respondent1OrganisationPolicy_OrgPolicyReference',
    searchText: '#search-org-text'
  },

  async enterOrganisationDetails() {
    I.waitForElement(this.fields.orgPolicyReference);
    I.fillField(this.fields.orgPolicyReference, 'Defendant policy reference');
    I.fillField(this.fields.searchText, 'Civil Damages Claims');
    I.click('a[title="Select the organisation Civil Damages Claims - Organisation 2"]');
    await I.clickContinue();
  }
};

