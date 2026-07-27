import ClaimType from '../../../../../constants/cases/claim-type';
import CCDCaseData from '../../../../../models/ccd-case-data';
import DefendantResponseType from '../../../../../constants/ccd-events/defendant-response/unspec/defendant-response-type';
import { Party } from '../../../../../models/users/partys';
import partys from '../../../../../constants/users/partys';

const confirmNameAddress = (
  ccdCaseData: CCDCaseData,
  defendantSolicitorParty: Party
) => {
  const respondent1 = structuredClone(ccdCaseData.respondent1);
  const respondent2 = structuredClone(ccdCaseData.respondent2);

  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      ConfirmNameAddress: {
        respondent1,
        respondent1Copy: respondent1,
        respondent2Copy: respondent2
      },
    };
  }
  
  else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      ConfirmNameAddress: {
        respondent2: structuredClone(ccdCaseData.respondent2),
        respondent1Copy: respondent1,
        respondent2Copy: respondent2,
      },
    };
  }
  
  return {};
};

const responseIntention = (
  claimType: ClaimType, 
  defendantResponseType: DefendantResponseType, 
  defendantSolicitorParty: Party 
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.TWO_VS_ONE) {
      return {
        ResponseIntention: {
          respondent1ClaimResponseIntentionType: defendantResponseType,
          respondent1ClaimResponseIntentionTypeApplicant2: defendantResponseType,
        },
      };
    } else if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        ResponseIntention: {
          respondent1ClaimResponseIntentionType: defendantResponseType,
          respondent2ClaimResponseIntentionType: defendantResponseType,
        },
      };
    }
    return {
      ResponseIntention: {
        respondent1ClaimResponseIntentionType: defendantResponseType,
      },
    }
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      ResponseIntention: {
        respondent2ClaimResponseIntentionType: defendantResponseType,
      },
    };
  }
};

const solicitorReferences = (
  ccdCaseData: CCDCaseData,
  claimType: ClaimType,
  defendantSolicitorParty: Party
) => {
  const solicitorReferences = structuredClone(ccdCaseData.solicitorReferences);

  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        SolicitorReferences: {
          solicitorReferences,
          solicitorReferencesCopy: {
            ...solicitorReferences,
            respondentSolicitor2Reference: solicitorReferences?.respondentSolicitor1Reference,
          },
        },
      };
    }

    return {
      SolicitorReferences: {
        solicitorReferences,
        solicitorReferencesCopy: solicitorReferences,
      },
    };
  }

  else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      SolicitorReferences: {
        respondentSolicitor2Reference: ccdCaseData?.respondentSolicitor2Reference,
        solicitorReferencesCopy: solicitorReferences,
      }
    };
  }
};

const acknowledgeClaimDataBuilderComponents = {
  confirmNameAddress,
  responseIntention,
  solicitorReferences,
};

export default acknowledgeClaimDataBuilderComponents;
