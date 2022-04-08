const {I} = inject();

module.exports = {
  fields: {
    selectLitigationFriend: {
      id: '#selectLitigationFriend',
      options: {
        both: 'Both',
        respondent1: 'Defendant One: Example respondent1 company',
        respondent2: 'Defendant Two: Example respondent2 company'
      }
    }
  },

  async selectDefendant(defendantWhoNeedsLitigant) {
    // eslint-disable-next-line no-prototype-builtins
    await this.checkOptionValidity(this.fields.selectLitigationFriend, defendantWhoNeedsLitigant);
    await this.inputResponse(this.fields.selectLitigationFriend, defendantWhoNeedsLitigant)
    await I.clickContinue();
  },

  async inputResponse(responseField, responseType) {
    I.waitForElement(responseField.id);
    I.selectOption(responseField.id, responseField.options[responseType]);
  },

  async checkOptionValidity(responseField, responseType) {
    if (!responseField.options.hasOwnProperty(responseType)) {
      throw new Error(`Option: ${responseType} does not exist`);
    }
  }
};

