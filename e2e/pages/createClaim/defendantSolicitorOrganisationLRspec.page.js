const { I } = inject();

module.exports = {

  fields: respondentNumber => {
    return {
      respondentOrgRepresented: {
        id: `#${respondentNumber}OrgRegistered`,
        options: {
          yes: `#${respondentNumber}OrgRegistered_Yes`,
          no: `#${respondentNumber}OrgRegistered_No`
        }
      },
      orgPolicyReference: `#${respondentNumber}OrganisationPolicy_OrgPolicyReference`,
      searchText: '#search-org-text',
    };
  },

  async enterOrganisationDetails (respondentNumber) {
    I.waitForElement(this.fields(respondentNumber).respondentOrgRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondentNumber).respondentOrgRepresented.id, () => {
      I.click(this.fields(respondentNumber).respondentOrgRepresented.options.yes);
    });
    I.waitForElement(this.fields(respondentNumber).orgPolicyReference);
    I.fillField(this.fields(respondentNumber).orgPolicyReference, 'Defendant policy reference');
    I.waitForElement(this.fields(respondentNumber).searchText);
    I.fillField(this.fields(respondentNumber).searchText, 'Civil');
    if (respondentNumber === 'respondent1') {
    I.click('a[title="Select the organisation Civil - Organisation 2"]');
    }
    if (respondentNumber === 'respondent2') {
      I.click('a[title="Select the organisation Civil - Organisation 3"]');
    }
    await I.clickContinue();
  }
};

