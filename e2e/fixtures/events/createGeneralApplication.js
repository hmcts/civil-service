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
        judgeRequiredYesOrNo: 'No',
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
  createGADataWithoutNotice: (isWithNotice, reasonWithoutNotice,calculatedAmount, code) => {
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
        judgeRequiredYesOrNo: 'No',
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
        judgeRequiredYesOrNo: 'No',
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
          calculatedAmountInPence: '27500',
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
  }
};
