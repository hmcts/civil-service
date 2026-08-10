const uuid = require('uuid');
const {date} = require('../../../api/dataHelper');
const docUuid = uuid.v1();
module.exports = {
  respondConsentGAData: (agree) => {
    return {
      gaRespondentConsent: agree ? 'Yes' : 'No',
      generalAppRespondConsentReason: agree ? null : 'Reason',
      generalAppRespondConsentDocument: agree ? null : [
        {
          id: docUuid,
          value: {
            document_url: '${TEST_DOCUMENT_URL}',
            document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
            document_filename: '${TEST_DOCUMENT_FILENAME}',
            documentHash: null
          }
        }
      ],
      hearingDetailsResp: {
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
      }
    };
  }, respondDebtorGAData: (agree) => {
    return {
      generalAppRespondent1Representative : {
        hasAgreed: agree ? 'Yes' : 'No',
      },
      gaRespondentDebtorOffer: agree ? null : {
        respondentDebtorOffer: 'DECLINE',
        paymentPlan: 'PAYFULL',
        debtorObjections: 'Objection Reason',
        paymentSetDate: date(1),
      },
      generalAppRespondDocument: agree ? null : [
        {
          id: docUuid,
          value: {
            document_url: '${TEST_DOCUMENT_URL}',
            document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
            document_filename: '${TEST_DOCUMENT_FILENAME}',
            documentHash: null
          }
        }
      ],
      hearingDetailsResp: {
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
      }
    };
  }, respondGAData: (agree) => {
    return {
      generalAppRespondent1Representative : {
        hasAgreed: agree ? 'Yes' : 'No',
      },
      generalAppRespondReason : agree ? null : 'Reason',
      generalAppRespondDocument: agree ? null : [
        {
          id: docUuid,
          value: {
            document_url: '${TEST_DOCUMENT_URL}',
            document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
            document_filename: '${TEST_DOCUMENT_FILENAME}',
            documentHash: null
          }
        }
      ],
      hearingDetailsResp: {
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
      }
    };
  }, toJudgeDirectionsOrders: () => {
    return {
      generalAppDirOrderUpload:[
       {
         id: docUuid,
         value: {
           document_url: '${TEST_DOCUMENT_URL}',
           document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
           document_filename: '${TEST_DOCUMENT_FILENAME}',
           documentHash: null
         }
      }
    ]
    };
  }, toJudgeAdditionalInfo: () => {
    return {
      generalAppAddlnInfoUpload:[
        {
          id: docUuid,
          value: {
            document_url: '${TEST_DOCUMENT_URL}',
            document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
            document_filename: '${TEST_DOCUMENT_FILENAME}',
            documentHash: null
          }
        }
      ]
    };
  }, toJudgeWrittenRepresentation: () => {
    return {
      generalAppWrittenRepUpload: [
        {
          id: docUuid,
          value: {
            document_url: '${TEST_DOCUMENT_URL}',
            document_binary_url: '${TEST_DOCUMENT_BINARY_URL}',
            document_filename: '${TEST_DOCUMENT_FILENAME}',
            documentHash: null
          }
        }
      ]
    };
  }
};
