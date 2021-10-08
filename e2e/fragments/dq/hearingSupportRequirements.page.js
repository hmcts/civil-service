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
    };
  },

  async selectRequirements(party) {
    I.waitForElement(this.fields(party).requirements.options.disabledAccess);
    await I.runAccessibilityTest();
    I.checkOption(this.fields(party).requirements.options.signLanguage);
    I.checkOption(this.fields(party).requirements.options.languageInterpreter);
    I.checkOption(this.fields(party).requirements.options.other);

    I.fillField(this.fields(party).signLanguageRequired, 'A language');
    I.fillField(this.fields(party).languageToBeInterpreted, 'A language');
    I.fillField(this.fields(party).otherSupport, 'Some support');
    await I.clickContinue();
  },
};
