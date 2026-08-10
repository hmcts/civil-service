const config = require('../../../config');
const {I} = inject();

module.exports = {

  fields: {
    judgeHearingLocation: '#judicialListForHearing_hearingPreferredLocation',
    hearingPreferences: {
      id: '#judicialListForHearing_hearingPreferencesPreferredType',
      options: {
        inPerson: 'In person',
        videoConferenceHearing: 'Video',
        telephoneHearing: 'Telephone'
      }
    },
    judicialTimeEstimate: {
      id: '#judicialListForHearing_judicialTimeEstimate',
      options: {
        fifteenMin: '15 minutes',
        thirtyMin: '30 minutes',
        fortyFiveMin: '45 minutes',
        oneHour: '1 hour',
        other: 'Other'
      }
    },
    judicialSupportRequirement: {
      id: '#judicialListForHearing_judicialSupportRequirement',
      options: {
        disabledAccess: 'Disabled access',
        hearingLoop: 'Hearing loop',
        signLanguageInterpreter: 'Sign language interpreter',
        languageInterpreter: 'Language interpreter',
        otherSupport: 'Other support'
      }
    },
    additionalInfoForCourtStaffTextArea: '#judicialListForHearing_addlnInfoCourtStaff',
  },


  async selectJudicialHearingPreferences(hearingPreferences) {
    I.seeInCurrentUrl('MAKE_DECISION/MAKE_DECISIONGAJudicialHearingDetailsScreen');
    I.waitForElement(this.fields.hearingPreferences.id);
    await within(this.fields.hearingPreferences.id, () => {
      I.click(this.fields.hearingPreferences.options[hearingPreferences]);
    });
    if ('inPerson' === hearingPreferences) {
      await I.see('Select an option from the dropdown');
    }
    if (!config.runWAApiTest) {
      await I.see('Applicant prefers In person. ' +
        'Respondent 1 prefers In person. ' +
        'Respondent 2 prefers In person.');
    }
  },

  async selectJudicialTimeEstimate(timeEstimate) {
    I.waitForElement(this.fields.judicialTimeEstimate.id);
    await within(this.fields.judicialTimeEstimate.id, () => {
      I.click(this.fields.judicialTimeEstimate.options[timeEstimate]);
    });
    if (!config.runWAApiTest) {
      await I.see('Applicant estimates 45 minutes. ' +
        'Respondent 1 estimates 45 minutes. ' +
        'Respondent 2 estimates 45 minutes.');
    }
  },

  async selectJudicialSupportRequirement(supportRequirement) {
    I.waitForElement(this.fields.judicialSupportRequirement.id);
    if (!config.runWAApiTest) {
      await I.see('Applicant requires Sign language interpreter. ' +
        'Respondent 1 requires Sign language interpreter. ' +
        'Respondent 2 requires Sign language interpreter.');
    }
    await within(this.fields.judicialSupportRequirement.id, () => {
      I.click(this.fields.judicialSupportRequirement.options[supportRequirement]);
    });
    await I.fillField(this.fields.additionalInfoForCourtStaffTextArea, 'Information for court staff');
    await I.click('Continue');
    await I.see('Select your preferred hearing location.');
    await I.seeNumberOfVisibleElements(this.fields.judgeHearingLocation, 1);
    await within(this.fields.hearingPreferences.id, () => {
      I.click(this.fields.hearingPreferences.options['videoConferenceHearing']);
    });
    await I.clickContinue();
  },

  async verifyVulnerabilityQuestions() {
    I.seeNumberOfVisibleElements('.case-field .case-field__value span', 5);
    await I.see('Applicant requires support with regards to vulnerability');
    await I.see('Test Test');
  },
};

