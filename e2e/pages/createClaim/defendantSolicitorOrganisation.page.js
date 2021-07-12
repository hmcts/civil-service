const { I } = inject();

module.exports = {

  fields: respondent => {
    return {
      respondentOrgRepresented: {
        id: `#${respondent}OrgRegistered`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      orgPolicyReference: `#${respondent}OrganisationPolicy_OrgPolicyReference`,
      searchText: '#search-org-text',
    };
  },

  async enterOrganisationDetails (respondent) {
    I.waitForElement(this.fields(respondent).respondentOrgRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondent).respondentOrgRepresented.id, () => {
      I.click(this.fields(respondent).respondentOrgRepresented.options.yes);
    });
    I.waitForElement(this.fields(respondent).orgPolicyReference);
    I.fillField(this.fields(respondent).orgPolicyReference, 'Defendant policy reference');
    I.waitForElement(this.fields(respondent).searchText);
    I.fillField(this.fields(respondent).searchText, 'Civil');
    I.click('a[title="Select the organisation Civil - Organisation 1"]');
    await I.clickContinue();
  },

  async enterOrganisationDetails2 (respondent) {
    I.waitForElement(this.fields(respondent).respondentOrgRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondent).respondentOrgRepresented.id, () => {
      I.click(this.fields(respondent).respondentOrgRepresented.options.yes);
    });
    I.waitForElement(this.fields(respondent).orgPolicyReference);
    I.fillField(this.fields(respondent).orgPolicyReference, 'Defendant policy reference');
    I.waitForElement(this.fields(respondent).searchText);
    I.fillField(this.fields(respondent).searchText, 'Civil');
    I.click('a[title="Select the organisation Civil - Organisation 2"]');
    await I.clickContinue();
  }
};

