const {nextYear} = require('../../../api/dataHelper');
const {I} = inject();

module.exports = {

  fields: {
    hearingScheduled: {
      id: '#hearingDetailsResp_hearingYesorNo',
      options: {
        yes: '#hearingDetailsResp_hearingYesorNo_Yes',
        no: '#hearingDetailsResp_hearingYesorNo_No'
      }
    },
    hearingDateDay: '#hearingDate-day',
    hearingDateMonth: '#hearingDate-month',
    hearingDateYear: '#hearingDate-year',
    judgeName: '#hearingDetailsResp_judgeName',
    trialRequired: {
      id: '#hearingDetailsResp_trialRequiredYesOrNo',
      options: {
        yes: '#hearingDetailsResp_trialRequiredYesOrNo_Yes',
        no: '#hearingDetailsResp_trialRequiredYesOrNo_No'
      }
    },
    trialDateFromDay: '#trialDateFrom-day',
    trialDateFromMonth: '#trialDateFrom-month',
    trialDateFromYear: '#trialDateFrom-year',
    trialDateToDay: '#trialDateTo-day',
    trialDateToMonth: '#trialDateTo-month',
    trialDateToYear: '#trialDateTo-year',
    hearingPreferences: {
      id: '#hearingDetailsResp_HearingPreferencesPreferredType',
      options: {
        inPerson: 'In person',
        videoConferenceHearing: 'Video conference hearing',
        telephoneHearing: 'Telephone hearing',
        withoutAHearing: 'Without a hearing'
      }
    },
    reasonForPreferredHearingType: '#hearingDetailsResp_ReasonForPreferredHearingType',
    hearingDetailsTelephoneNumber: '#hearingDetailsResp_HearingDetailsTelephoneNumber',
    hearingDetailsEmailID: '#hearingDetailsResp_HearingDetailsEmailID',
    hearingDuration: {
      id: '#hearingDetailsResp_HearingDuration',
      options: {
        fifteenMin: '15 minutes',
        thirtyMin: '30 minutes',
        fortyFiveMin: '45 minutes',
        other: 'Other'
      }
    },
    unavailableTrailRequired: {
      id: '#hearingDetailsResp_unavailableTrialRequiredYesOrNo',
      options: {
        yes: '#hearingDetailsResp_unavailableTrialRequiredYesOrNo_Yes',
        no: '#hearingDetailsResp_unavailableTrialRequiredYesOrNo_No'
      }
    },
    unavailableDateFromDay: '#unavailableTrialDateFrom-day',
    unavailableDateFromMonth: '#unavailableTrialDateFrom-month',
    unavailableDateFromYear: '#unavailableTrialDateFrom-year',
    unavailableDateToDay: '#unavailableTrialDateTo-day',
    unavailableDateToMonth: '#unavailableTrialDateTo-month',
    unavailableDateToYear: '#unavailableTrialDateTo-year',
    supportRequirement: {
      id: '#hearingDetailsResp_SupportRequirement',
      options: {
        disabledAccess: 'Disabled access',
        hearingLoop: 'Hearing loop',
        signLanguageInterpreter: 'Sign language interpreter (you will have to arrange this)',
        languageInterpreter: 'Language interpreter (you will have to arrange this)',
        otherSupport: 'Other support'
      }
    },
    vulnerabilityQuestions: {
      id: '#hearingDetailsResp_vulnerabilityQuestionsYesOrNo',
      options: {
        yes: '#hearingDetailsResp_vulnerabilityQuestionsYesOrNo_Yes',
        no: '#hearingDetailsResp_vulnerabilityQuestionsYesOrNo_No'
      }
    },
    vulnerabilityQuestionTextArea: '#hearingDetailsResp_vulnerabilityQuestion',
    supportRequirementSignLanguage: '#hearingDetailsResp_SupportRequirementSignLanguage',
  },

  async isRespHearingScheduled(hearingScheduledCheck) {
    I.waitForElement(this.fields.hearingScheduled.id);
    I.seeInCurrentUrl('RESPOND_TO_APPLICATION/RESPOND_TO_APPLICATIONGARespHearingScreen');
    await within(this.fields.hearingScheduled.id, () => {
      I.click(this.fields.hearingScheduled.options[hearingScheduledCheck]);
    });
    if ('yes' === hearingScheduledCheck) {
      await I.fillField(this.fields.hearingDateDay, 1);
      await I.fillField(this.fields.hearingDateMonth, 10);
      await I.fillField(this.fields.hearingDateYear, nextYear());
    }
  },

  async isRespTrialRequired(trialRequired) {
    I.waitForElement(this.fields.trialRequired.id);
    await within(this.fields.trialRequired.id, () => {
      I.click(this.fields.trialRequired.options[trialRequired]);
    });
    if ('yes' === trialRequired) {
      await I.fillField(this.fields.trialDateFromDay, 1);
      await I.fillField(this.fields.trialDateFromMonth, 10);
      await I.fillField(this.fields.trialDateFromYear, nextYear());
      await I.fillField(this.fields.trialDateToDay, 1);
      await I.fillField(this.fields.trialDateToMonth, 12);
      await I.fillField(this.fields.trialDateToYear, nextYear());
    }
  },

  async selectRespHearingPreferences(hearingPreferences) {
    I.waitForElement(this.fields.hearingPreferences.id);
    await within(this.fields.hearingPreferences.id, () => {
      I.click(this.fields.hearingPreferences.options[hearingPreferences]);
    });
    await I.fillField(this.fields.reasonForPreferredHearingType, 'Test Test');
    await I.fillField(this.fields.hearingDetailsTelephoneNumber, '07446775177');
    await I.fillField(this.fields.hearingDetailsEmailID, 'test@gmail.com');
  },

  async selectRespHearingDuration(hearingDuration) {
    I.waitForElement(this.fields.hearingDuration.id);
    await within(this.fields.hearingDuration.id, () => {
      I.click(this.fields.hearingDuration.options[hearingDuration]);
    });
  },

  async isRespUnavailableTrailRequired(trailRequired) {
    I.waitForElement(this.fields.unavailableTrailRequired.id);
    await within(this.fields.unavailableTrailRequired.id, () => {
      I.click(this.fields.unavailableTrailRequired.options[trailRequired]);
    });
    if ('yes' === trailRequired) {
      I.wait(1);
      I.click({css: '#hearingDetailsResp_generalAppUnavailableDates .button:nth-child(2)'});
      I.waitForVisible(this.fields.unavailableDateFromDay);
      I.fillField(this.fields.unavailableDateFromDay, 1);
      I.fillField(this.fields.unavailableDateFromMonth, 10);
      I.fillField(this.fields.unavailableDateFromYear, nextYear());
      I.fillField(this.fields.unavailableDateToDay, 1);
      I.fillField(this.fields.unavailableDateToMonth, 12);
      I.fillField(this.fields.unavailableDateToYear, nextYear());
    }
  },

  async selectRespVulnerabilityQuestions(vulnerabilityQuestions) {
    I.waitForElement(this.fields.vulnerabilityQuestions.id);
    await within(this.fields.vulnerabilityQuestions.id, () => {
      I.click(this.fields.vulnerabilityQuestions.options[vulnerabilityQuestions]);
    });
    if ('yes' === vulnerabilityQuestions) {
      await I.fillField(this.fields.vulnerabilityQuestionTextArea, 'Test Test');
    }
  },

  async selectRespSupportRequirement(supportRequirement) {
    I.waitForElement(this.fields.supportRequirement.id);
    await within(this.fields.supportRequirement.id, () => {
      I.click(this.fields.supportRequirement.options[supportRequirement]);
    });
    if ('signLanguageInterpreter' === supportRequirement) {
      await I.fillField(this.fields.supportRequirementSignLanguage, 'SignLanguage');
    }
    await I.clickContinue();
  },
};

