const {I} = inject();

const options = {
  fullDefence: 'Reject all of the claim',
  fullAdmission: 'Admit all of the claim',
  partAdmission: 'Admit part of the claim',
  counterClaim: 'Reject all of the claim and wants to counterclaim'
};

module.exports = {
  fields: (party) => ({
    respondentClaimResponseType: {
      id: `#${party}ClaimResponseType`,
      options
    },
    respondentClaimResponseTypeToApplicant2: {
      id: `#${party}ClaimResponseTypeToApplicant2`,
      options
    },
  }),

  async selectResponseType({defendant1Response, defendant2Response, defendant1ResponseToApplicant2}) {
    // eslint-disable-next-line no-prototype-builtins
    if(defendant1Response) {
      await this.inputResponse(this.fields('respondent1').respondentClaimResponseType, defendant1Response);
    }

    if(defendant2Response) {
      await this.inputResponse(this.fields('respondent2').respondentClaimResponseType, defendant2Response);
    }

    if(defendant1ResponseToApplicant2) {
      await this.inputResponse(this.fields('respondent1').respondentClaimResponseTypeToApplicant2, defendant1ResponseToApplicant2);
    }
    await I.clickContinue();
  },

  async inputResponse(responseField, responseType) {
    await this.checkResponseValidity(responseField, responseType);
    I.waitForElement(responseField.id);
    await I.runAccessibilityTest();
    await within(responseField.id, () => {
      I.click(responseField.options[responseType]);
    });
  },

  async checkResponseValidity(responseField, responseType) {
    if (!Object.prototype.hasOwnProperty.call(responseField.options, responseType)) {
      throw new Error(`Response type: ${responseType} does not exist`);
    }
  }
};

