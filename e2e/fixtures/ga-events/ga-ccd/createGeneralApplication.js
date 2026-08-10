const {date} = require('../../../api/dataHelper');
const config = require('../../../config.js');

module.exports = {
  createGAData: (isWithNotice, reasonWithoutNotice, calculatedAmount, code) => {
    return {
      generalAppType: {
        types: [
          'STRIKE_OUT',
          'SUMMARY_JUDGEMENT',
          'EXTEND_TIME'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: isWithNotice,
        reasonsForWithoutNotice: reasonWithoutNotice
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Answer',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },
  createGADataVaryJudgement: (isWithNotice, reasonWithoutNotice, calculatedAmount, code, linkGAN245FormUpload, urgency) => {
    return {
      generalAppType: {
        types: [
          'VARY_PAYMENT_TERMS_OF_JUDGMENT'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: urgency ? 'Yes' : 'No',
        urgentAppConsiderationDate: urgency ? date(1) : null,
        reasonsForUrgency: urgency ? 'Urgent!' : null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: isWithNotice,
        reasonsForWithoutNotice: reasonWithoutNotice
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Answer',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppN245FormUpload: linkGAN245FormUpload,
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },
  createGADataWithoutNotice: (isWithNotice, reasonWithoutNotice, calculatedAmount, code) => {
    return {
      generalAppType: {
        types: [
          'SUMMARY_JUDGEMENT',
          'EXTEND_TIME'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      respondent2OrganisationPolicy: {
        OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORTWO]',
        Organisation: {
          OrganisationID: config.defendant2SolicitorOrgId,
          OrganisationName: 'Civil - Organisation 2'
        }
      },
      respondent1OrganisationPolicy: {
        OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
        Organisation: {
          OrganisationID: config.defendant1SolicitorOrgId,
          OrganisationName: 'Civil - Organisation 1'
        }
      },
      generalAppInformOtherParty: {
        isWithNotice: isWithNotice,
        reasonsForWithoutNotice: reasonWithoutNotice
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Test',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },
  gaTypeWithStayClaim: () => {
    return {
      generalAppType: {
        types: [
          'STAY_THE_CLAIM'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: 'Yes',
        reasonsForWithoutNotice: null
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Test',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: '30300',
          code: 'FEE0442',
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },

  gaTypeWithUnlessOrder: () => {
    return {
      generalAppType: {
        types: [
          'UNLESS_ORDER'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: 'Yes',
        reasonsForWithoutNotice: null
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Answer',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: '30300',
          code: 'FEE0442',
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },

  gaTypeWithNoStrikeOut: () => {
    return {
      generalAppType: {
        types: [
          'EXTEND_TIME'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: 'Yes',
        reasonsForWithoutNotice: null
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Answer',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: '30300',
          code: 'FEE0442',
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },
  createGaAdjournVacateData: (isWithNotice, isWithConsent, hearingDate, calculatedAmount, code, version) => {
    return {
      generalAppType: {
        types: [
          'ADJOURN_HEARING'
        ]
      },
      generalAppRespondentAgreement: {
        hasAgreed: isWithConsent
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppHearingDate: {
        hearingScheduledDate: hearingDate,
        hearingScheduledPreferenceYesNo: 'Yes'
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppInformOtherParty: {
        isWithNotice: isWithNotice,
        reasonsForWithoutNotice: 'reason'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Answer',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: version
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },

  createGA: (gaTypes, isWithNotice, reasonWithoutNotice, calculatedAmount, code) => {
    return {
      generalAppType: {
        types: gaTypes,
      },
      generalAppRespondentAgreement: {
        hasAgreed: 'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: 'No',
        urgentAppConsiderationDate: null,
        reasonsForUrgency: null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: {
        isWithNotice: isWithNotice,
        reasonsForWithoutNotice: reasonWithoutNotice
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Test',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },

  createGeneralAppN245FormUpload: (document) => {
    return {
      document_url: document.document_url,
      document_binary_url: document.document_binary_url,
      document_filename: document.document_filename,
      category_id: 'applications'
    };
  },

  createGaWithConsentAndNotice: (gaTypes, consent, urgency, withNotice, calculatedAmount, code) => {
    return {
      generalAppType: {
        types: gaTypes,
      },
      generalAppRespondentAgreement: {
        hasAgreed: consent ? 'Yes':'No'
      },
      generalAppUrgencyRequirement: {
        generalAppUrgency: urgency ? 'Yes' : 'No',
        urgentAppConsiderationDate: urgency ? date(1) : null,
        reasonsForUrgency: urgency ? 'Urgent!' : null,
        ConsentAgreementCheckBox: []
      },
      generalAppInformOtherParty: withNotice == null ? null : {
        isWithNotice: withNotice ? 'Yes':'No',
        reasonsForWithoutNotice: withNotice ? 'Reason':null,
      },
      generalAppDetailsOfOrder: 'Test Order details',
      generalAppReasonsOfOrder: 'Test reason for order',
      generalAppEvidenceDocument: [],
      generalAppStatementOfTruthConsent: [
        'ConsentAgreementCheckBox'
      ],
      generalAppStatementOfTruth: {
        name: 'John Doe',
        role: 'Test Solicitor'
      },
      generalAppHearingDetails: {
        hearingYesorNo: 'No',
        hearingDate: null,
        judgeName: null,
        trialRequiredYesOrNo: 'No',
        trialDateFrom: null,
        trialDateTo: null,
        HearingPreferencesPreferredType: 'IN_PERSON',
        TelephoneHearingPreferredType: null,
        ReasonForPreferredHearingType: 'sdsd',
        HearingPreferredLocation: null,
        HearingDetailsTelephoneNumber: '07446778166',
        HearingDetailsEmailID: 'update@gh.com',
        HearingDuration: 'MINUTES_15',
        generalAppHearingDays: null,
        generalAppHearingHours: null,
        generalAppHearingMinutes: null,
        unavailableTrialRequiredYesOrNo: 'No',
        vulnerabilityQuestionsYesOrNo: 'Yes',
        vulnerabilityQuestion: 'Test Test',
        SupportRequirementSignLanguage: null,
        SupportRequirementLanguageInterpreter: null,
        SupportRequirementOther: null,
        generalAppUnavailableDates: [],
        SupportRequirement: []
      },
      generalAppPBADetails: {
        paymentSuccessfulDate: null,
        fee: {
          calculatedAmountInPence: calculatedAmount,
          code: code,
          version: '2'
        },
        paymentDetails: {
          status: null,
          reference: null,
          errorMessage: null,
          errorCode: null,
          customerReference: null
        },
        serviceRequestReference: null
      }
    };
  },
};
