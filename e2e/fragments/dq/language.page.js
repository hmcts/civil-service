const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      evidence: {
        id: `#${party}DQLanguage_evidence`,
        options: {
          welsh: `#${party}DQLanguage_evidence-WELSH`,
        }
      },
      court: {
        id: `#${party}DQLanguage_court`,
        options: {
          welsh: `#${party}DQLanguage_court-WELSH`,
        }
      },
      documents: {
        id: `#${party}DQLanguage_documents`,
        options: {
          welsh: `#${party}DQLanguage_documents-WELSH`,
        }
      },
    };
  },

  async enterWelshLanguageRequirements(party) {
    I.waitForElement(this.fields(party).evidence.id);
    await I.runAccessibilityTest();

    I.click(this.fields(party).evidence.options.welsh);
    I.click(this.fields(party).court.options.welsh);
    I.click(this.fields(party).documents.options.welsh);

    await I.clickContinue();
  },
};
