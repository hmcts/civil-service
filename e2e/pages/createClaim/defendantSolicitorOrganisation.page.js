const { I } = inject();

module.exports = {

  fields: respondentNumber => {
    return {
      respondentOrgRepresented: {
        id: `#respondent${respondentNumber}OrgRegistered`,
        options: {
          yes: `#respondent${respondentNumber}OrgRegistered_Yes`,
          no: `#respondent${respondentNumber}OrgRegistered_No`
        }
      },
      orgPolicyReference: `#respondent${respondentNumber}OrganisationPolicy_OrgPolicyReference`,
      searchText: '#search-org-text',
    };
  },

  async enterOrganisationDetails (respondentNumber, organisationNumber = 1) {
    await I.runAccessibilityTest();
    I.waitForElement(this.fields(respondentNumber).orgPolicyReference);
    I.fillField(this.fields(respondentNumber).orgPolicyReference, 'Defendant policy reference');
    I.waitForElement(this.fields(respondentNumber).searchText);
    I.fillField(this.fields(respondentNumber).searchText, 'Civil');
    I.click(`a[title="Select the organisation Civil - Organisation ${organisationNumber}"]`);
    await I.clickContinue();
  }
};

