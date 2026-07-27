import ClaimType from "../../../../../constants/cases/claim-type";
import partys from "../../../../../constants/users/partys";
import CaseDataHelper from "../../../../../helpers/case-data-helper";
import DateHelper from "../../../../../helpers/date-helper";
import { UploadDocumentValue } from "../../../../../models/ccd-case-data";
import { ClaimantDefendantPartyType } from "../../../../../models/users/claimant-defendant-party-types";
import { Party } from "../../../../../models/users/partys";

const explanation = {
  Explanation: {}
};

const whoIsDocumentFor = (
    claimType: ClaimType, 
    claimantDefendantSolicitorParty: Party, 
    claimant1PartyType: ClaimantDefendantPartyType, 
    defendant1PartyType: ClaimantDefendantPartyType,
    defendant2PartyType: ClaimantDefendantPartyType,
  ) => {
  if (claimantDefendantSolicitorParty === partys.CLAIMANT_SOLICITOR_1) {
    if (claimType === ClaimType.TWO_VS_ONE) {
      return {
        WhoIsDocumentFor: {
          uploadMediationDocumentsPartyChosen: {
            list_items: [{
              code: 'CLAIMANTS',
              label: 'Claimants 1 and 2'
            }],
            value: {
              code: 'CLAIMANTS',
              label: 'Claimants 1 and 2'
            }
          }
        }
      }
    }
    return {
      WhoIsDocumentFor: {
        uploadMediationDocumentsPartyChosen: {
          list_items: [{
            code: 'CLAIMANT_1',
            label: `Claimant 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType)}`
          }],
          value: {
            code: 'CLAIMANT_1',
            label: `Claimant 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimant1PartyType)}`
          }
        }
      }
    }
  }
  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) 
      return {
        WhoIsDocumentFor: {
          uploadMediationDocumentsPartyChosen: {
            list_items: [{
              code: 'DEFENDANTS',
              label: 'Defendants 1 and 2'
            }],
            value: {
              code: 'DEFENDANTS',
              label: 'Defendants 1 and 2'
            }
          }
        }
      };
      
    return {
      WhoIsDocumentFor: {
        uploadMediationDocumentsPartyChosen: {
          list_items: [{
            code: 'DEFENDANT_1',
            label: `Defendant 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1PartyType).partyName}`
          }],
          value: {
            code: 'DEFENDANT_1',
            label: `Defendant 1: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendant1PartyType).partyName}`
          }
        }
      }
    };
  }
  else if (claimantDefendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      WhoIsDocumentFor: {
        uploadMediationDocumentsPartyChosen: {
          list_items: [{
            code: 'DEFENDANT_2',
            label: `Defendant 2: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_2, defendant2PartyType).partyName}`
          }],
          value: {
            code: 'DEFENDANT_2',
            label: `Defendant 2: ${CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_2, defendant2PartyType).partyName}`
          }
        }
      }
    }
  }
}

const documentType = {
  DocumentType: {
    mediationDocumentsType: [
      'NON_ATTENDANCE_STATEMENT',
      'REFERRED_DOCUMENTS'
    ]
  }
}

const documentUpload = (nonAttendanceStatementDoc: UploadDocumentValue, referredDoc: UploadDocumentValue, claimantDefendantSolicitorParty: Party) => ({
  DocumentUpload: {
    nonAttendanceStatementForm: 
    [
      CaseDataHelper.setIdToData({
        yourName: `Your name - ${claimantDefendantSolicitorParty.key}`,
        documentDate: DateHelper.formatDateToString(DateHelper.subtractFromToday({ months: 1 }), {outputFormat: 'YYYY-MM-DD'}),
        document: nonAttendanceStatementDoc
      })
    ],
    documentsReferredForm: 
    [
      CaseDataHelper.setIdToData({
        documentType: `Non Attendance Statement Doc - ${claimantDefendantSolicitorParty.key}`,
        documentDate: DateHelper.formatDateToString(DateHelper.subtractFromToday({ months: 2 }), {outputFormat: 'YYYY-MM-DD'}),
        document: referredDoc
      })
    ]
  }
});

const uploadMediationDocumentsDataBuilderComponents = {
  explanation,
  whoIsDocumentFor,
  documentType,
  documentUpload
};

export default uploadMediationDocumentsDataBuilderComponents;