const {I} = inject();

module.exports = {
  fields: {
    defendantSolicitorNotifyClaimOptions: {
      id: '#defendantSolicitorNotifyClaimOptions',
      options: {
        both: 'Both',
        solicitor1: 'Defendant One: Example respondent1 company',
        solicitor2: 'Defendant Two: Example respondent2 company'
      }
    }
  },

  async selectSolicitorToNotify(solicitorToNotify) {
    // eslint-disable-next-line no-prototype-builtins
    await this.checkOptionValidity(this.fields.defendantSolicitorNotifyClaimOptions, solicitorToNotify);
    await this.inputResponse(this.fields.defendantSolicitorNotifyClaimOptions, solicitorToNotify);
    await I.clickContinue();
  },

  async inputResponse(responseField, responseType) {
      I.waitForElement(responseField.id);
      I.selectOption(responseField.id, responseField.options[responseType]);
  },

  async checkOptionValidity(responseField, responseType) {
    if (!Object.prototype.hasOwnProperty.call(responseField.options, responseType)) {
      throw new Error(`Option: ${responseType} does not exist`);
    }
  }
};

