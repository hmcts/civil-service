const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      requirements: {
        options: {
          disabledAccess: `#${party}DQHearingSupport_requirements-DISABLED_ACCESS`,
          hearingLoop: `#${party}DQHearingSupport_requirements-HEARING_LOOPS`,
          signLanguage: `#${party}DQHearingSupport_requirements-SIGN_INTERPRETER`,
          languageInterpreter: `#${party}DQHearingSupport_requirements-LANGUAGE_INTERPRETER`,
          other: `#${party}DQHearingSupport_requirements-OTHER_SUPPORT`
        }
      },
      signLanguageRequired: `#${party}DQHearingSupport_signLanguageRequired`,
      languageToBeInterpreted: `#${party}DQHearingSupport_languageToBeInterpreted`,
      otherSupport: `#${party}DQHearingSupport_otherSupport`,
      supportRequirements: {
        id: `#${party}DQHearingSupport_supportRequirements`,
        options: {
          yes: `#${party}DQHearingSupport_supportRequirements_Yes`,
          no: `#${party}DQHearingSupport_supportRequirements_No`
        }
      },
      supportRequirementsAdditional: `#${party}DQHearingSupport_supportRequirementsAdditional`
    };
  },

  async selectRequirements(party) {
    I.waitForElement(this.fields(party).supportRequirements.id);
    await I.runAccessibilityTest();
    await within(this.fields(party).supportRequirements.id, () => {
      I.click(this.fields(party).supportRequirements.options.yes);
    });
    I.fillField(this.fields(party).supportRequirementsAdditional, 'Reason for support');
    await I.clickContinue();
  },
};
