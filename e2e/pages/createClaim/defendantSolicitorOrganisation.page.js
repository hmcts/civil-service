const { I } = inject();

module.exports = {

  fields: {
    respondent1OrgRepresented: {
      id: '#respondent1OrgRegistered',
      options: {
        yes: 'Yes',
        no: 'No'
      }
    },
    orgPolicyReference: '#respondent1OrganisationPolicy_OrgPolicyReference',
    searchText: '#search-org-text',
  },

  async enterOrganisationDetails () {
    I.waitForElement(this.fields.respondent1OrgRepresented.id);
    await within(this.fields.respondent1OrgRepresented.id, () => {
      I.click(this.fields.respondent1OrgRepresented.options.yes);
    });
    I.waitForElement(this.fields.orgPolicyReference);
    I.fillField(this.fields.orgPolicyReference, 'Defendant policy reference');
    I.fillField(this.fields.searchText, 'Civil Damages Claims');
    I.click('a[title="Select the organisation Civil Damages Claims - Organisation 2"]');
    await I.clickContinue();
  },

  async organisationNotRegisteredInMyHMCTS () {
    I.waitForElement(this.fields.respondent1OrgRepresented.id);
    await within(this.fields.respondent1OrgRepresented.id, () => {
      I.click(this.fields.respondent1OrgRepresented.options.no);
    });
    await I.clickContinue();
  }
};

