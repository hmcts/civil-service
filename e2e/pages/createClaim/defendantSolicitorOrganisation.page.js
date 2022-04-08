const { I } = inject();

module.exports = {

  fields: respondentNumber => {
    return {
      respondentOrgRepresented: {
        id: `#respondent${respondentNumber}OrgRegistered`,
        options: {
          yes: 'Yes',
          no: 'No'
        }
      },
      orgPolicyReference: `#respondent${respondentNumber}OrganisationPolicy_OrgPolicyReference`,
      searchText: '#search-org-text',
    };
  },

  async enterOrganisationDetails (organisationRegistered = true, respondentNumber, organisationNumber = 1) {
    I.waitForElement(this.fields(respondentNumber).respondentOrgRepresented.id);
    await I.runAccessibilityTest();
    await within(this.fields(respondentNumber).respondentOrgRepresented.id, () => {
      const {yes, no} = this.fields(respondentNumber).respondentOrgRepresented.options;
      I.click(organisationRegistered ? yes : no);
    });

    if (organisationRegistered) {
      I.waitForElement(this.fields(respondentNumber).orgPolicyReference);
      I.fillField(this.fields(respondentNumber).orgPolicyReference, 'Defendant policy reference');
      I.waitForElement(this.fields(respondentNumber).searchText);
      I.fillField(this.fields(respondentNumber).searchText, 'Civil');
      I.click(`a[title="Select the organisation Civil - Organisation ${organisationNumber}"]`);
    }
    await I.clickContinue();
  }
};

