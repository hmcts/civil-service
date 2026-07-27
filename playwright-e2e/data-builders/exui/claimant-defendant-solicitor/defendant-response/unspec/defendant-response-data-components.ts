import preferredCourts from '../../../../../config/preferred-courts';
import {
  defendantSolicitor1User,
  defendantSolicitor2User,
} from '../../../../../config/users/exui-users';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import DefendantResponseType from '../../../../../constants/ccd-events/defendant-response/unspec/defendant-response-type';
import partys from '../../../../../constants/users/partys';
import DateHelper from '../../../../../helpers/date-helper';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import CCDCaseData, { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import { Party } from '../../../../../models/users/partys';
import ClaimType from '../../../../../constants/cases/claim-type';

const confirmDetails = (
  claimType: ClaimType,
  ccdCaseData: CCDCaseData,
  defendantSolicitorParty: Party,
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        ConfirmDetails: {
          respondent1: structuredClone(ccdCaseData.respondent1),
          respondent1Copy: structuredClone(ccdCaseData.respondent1),
          respondent2Copy: structuredClone(ccdCaseData.respondent2),
        },
      };
    }
    return {
      ConfirmDetails: {
        respondent1: structuredClone(ccdCaseData.respondent1),
        respondent1Copy: structuredClone(ccdCaseData.respondent1),
      },
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      ConfirmDetails: {
        respondent2: structuredClone(ccdCaseData.respondent2),
        respondent2Copy: structuredClone(ccdCaseData.respondent2),
      },
    };
  }

  return {};
};

const singleResponse = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      SingleResponse: {
        respondentResponseIsSame: 'Yes',
      },
    };
  }

  return {};
};

const respondentResponseType = (
  claimType: ClaimType,
  responseType: DefendantResponseType,
  defendantSolicitorParty: Party,
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
      return {
        RespondentResponseType: {
          respondent1ClaimResponseType: responseType,
        },
      };
    } else if (claimType === ClaimType.TWO_VS_ONE) {
      return {
        RespondentResponseType: {
          respondent1ClaimResponseType: responseType,
          respondent1ClaimResponseTypeToApplicant2: responseType,
        },
      };
    }
    return {
      RespondentResponseType: {
        respondent1ClaimResponseType: responseType,
        multiPartyResponseTypeFlags: responseType,
      },
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      RespondentResponseType: {
        respondent2ClaimResponseType: responseType,
      },
    };
  }

  return {};
};

const solicitorReferences = (ccdCaseData: CCDCaseData, defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      SolicitorReferences: {
        solicitorReferences: {
          respondentSolicitor1Reference:
            ccdCaseData?.solicitorReferences?.respondentSolicitor1Reference,
        },
      },
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      SolicitorReferences: {
        respondentSolicitor2Reference: ccdCaseData?.respondentSolicitor2Reference,
      },
    };
  }

  return {};
};

const deterWithoutHearing = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (claimTrack === ClaimTrack.SMALL_CLAIM) {
    return {
      DeterminationWithoutHearing: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'deterWithoutHearingRespondent1'
          : 'deterWithoutHearingRespondent2']: {
          deterWithoutHearingWhyNot: `Determination without hearing reason - ${defendantSolicitorParty.key}`,
          deterWithoutHearingYesNo: 'No',
        },
      },
    };
  }

  return {};
};

const upload = (defenceDocument: UploadDocumentValue, defendantSolicitorParty: Party) => {
  return {
    Upload: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1ClaimResponseDocument'
        : 'respondent2ClaimResponseDocument']: {
        file: defenceDocument,
      },
    },
  };
};

const fileDirectionsQuestionnaire = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM
  ) {
    return {
      FileDirectionsQuestionnaire: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQFileDirectionsQuestionnaire'
          : 'respondent2DQFileDirectionsQuestionnaire']: {
          explainedToClient: ['CONFIRM'],
          oneMonthStayRequested: 'No',
          reactionProtocolCompliedWith: 'No',
          reactionProtocolNotCompliedWithReason: `No explanation - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const fixedRecoverableCosts = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM) {
    return {
      FixedRecoverableCosts: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQFixedRecoverableCosts'
          : 'respondent2DQFixedRecoverableCosts']: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_2',
          complexityBandingAgreed: 'No',
          reasons: `No explanation - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const fixedRecoverableCostsIntermediate = (
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
  frcSupportingDocument?: UploadDocumentValue,
) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      FixedRecoverableCostsIntermediate: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQFixedRecoverableCostsIntermediate'
          : 'respondent2DQFixedRecoverableCostsIntermediate']: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_2',
          complexityBandingAgreed: 'No',
          reasons: `No explanation - ${defendantSolicitorParty.key}`,
          frcSupportingDocument,
        },
      },
    };
  }

  return {};
};

const disclosureOfElectronicDocuments = (
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      DisclosureOfElectronicDocuments: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQDisclosureOfElectronicDocuments'
          : 'respondent2DQDisclosureOfElectronicDocuments']: {
          reachedAgreement: 'No',
          agreementLikely: 'Yes',
        },
      },
    };
  }

  return {};
};

const disclosureOfNonElectronicDocuments = (
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM
  ) {
    return {
      DisclosureOfNonElectronicDocuments: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQDisclosureOfNonElectronicDocuments'
          : 'respondent2DQDisclosureOfNonElectronicDocuments']: {
          directionsForDisclosureProposed: 'Yes',
          standardDirectionsRequired: 'No',
          bespokeDirections: `No directions required - ${defendantSolicitorParty.key}`,
        },
      },
    };
  }

  return {};
};

const experts = (defendantSolicitorParty: Party) => {
  const defendantExperts =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
      ? [partys.DEFENDANT_2_EXPERT_1, partys.DEFENDANT_2_EXPERT_2]
      : [partys.DEFENDANT_1_EXPERT_1, partys.DEFENDANT_1_EXPERT_2];

  return {
    Experts: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQExperts'
        : 'respondent1DQExperts']: {
        expertRequired: 'Yes',
        expertReportsSent: 'NOT_OBTAINED',
        jointExpertSuitable: 'Yes',
        details: defendantExperts.map((expertParty) =>
          CaseDataHelper.setIdToData({
            ...CaseDataHelper.buildExpertData(expertParty),
            partyName: undefined,
          }),
        ),
      },
    },
  };
};

const witnesses = (defendantSolicitorParty: Party) => {
  const defendantWitnesses =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
      ? [partys.DEFENDANT_2_WITNESS_1, partys.DEFENDANT_2_WITNESS_2]
      : [partys.DEFENDANT_1_WITNESS_1, partys.DEFENDANT_1_WITNESS_2];

  return {
    Witnesses: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQWitnesses'
        : 'respondent1DQWitnesses']: {
        witnessesToAppear: 'Yes',
        details: defendantWitnesses.map((witnessParty) =>
          CaseDataHelper.setIdToData({
            ...CaseDataHelper.buildWitnessData(witnessParty),
            partyName: undefined,
          }),
        ),
      },
    },
  };
};

const language = (defendantSolicitorParty: Party) => {
  return {
    Language: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQLanguage'
        : 'respondent2DQLanguage']: {
        court: 'BOTH',
        documents: 'BOTH',
      },
    },
  };
};

const hearing = (defendantSolicitorParty: Party) => {
  return {
    Hearing: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQHearing'
        : 'respondent2DQHearing']: {
        unavailableDatesRequired: 'Yes',
        unavailableDates: [
          CaseDataHelper.setIdToData({
            unavailableDateType: 'SINGLE_DATE',
            date: DateHelper.formatDateToString(DateHelper.addToToday({ months: 6 }), {
              outputFormat: 'YYYY-MM-DD',
            }),
          }),
        ],
      },
    },
  };
};

const draftDirections = (
  claimTrack: ClaimTrack,
  draftDirectionsDocument: UploadDocumentValue,
  defendantSolicitorParty: Party,
) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM || claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      DraftDirections: {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQDraftDirections'
          : 'respondent2DQDraftDirections']: draftDirectionsDocument,
      },
    };
  }

  return {};
};

const requestedCourt = (defendantSolicitorParty: Party) => {
  const preferredCourtParty =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
      ? partys.DEFENDANT_1
      : partys.DEFENDANT_2;
  const preferredCourt = CaseDataHelper.setCodeToData(
    preferredCourts[preferredCourtParty.key].default,
  );

  return {
    RequestedCourt: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQRequestedCourt'
        : 'respondent2DQRequestedCourt']: {
        responseCourtLocations: {
          list_items: [preferredCourt],
          value: preferredCourt,
        },
        reasonForHearingAtSpecificCourt: `Reason for preferred court - ${defendantSolicitorParty.key}`,
      },
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQRemoteHearing'
        : 'respondent2DQRemoteHearing']: {
        remoteHearingRequested: 'Yes',
        reasonForRemoteHearing: `Reason for remote hearing - ${defendantSolicitorParty.key}`,
      },
    },
  };
};

const hearingSupport = (defendantSolicitorParty: Party) => {
  return {
    HearingSupport: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQHearingSupport'
        : 'respondent2DQHearingSupport']: {
        supportRequirements: 'Yes',
        supportRequirementsAdditional: `Support requirements for ${defendantSolicitorParty.key}`,
      },
    },
  };
};

const vulnerabilityQuestions = (defendantSolicitorParty: Party) => {
  return {
    VulnerabilityQuestions: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQVulnerabilityQuestions'
        : 'respondent2DQVulnerabilityQuestions']: {
        vulnerabilityAdjustmentsRequired: 'Yes',
        vulnerabilityAdjustments: `Vulnerability adjustments - ${defendantSolicitorParty.key}`,
      },
    },
  };
};

const furtherInformation = (defendantSolicitorParty: Party) => {
  return {
    FurtherInformation: {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQFurtherInformation'
        : 'respondent2DQFurtherInformation']: {
        futureApplications: 'Yes',
        reasonForFutureApplications: `Further information - ${defendantSolicitorParty.key}`,
        otherInformationForJudge: `Further information - ${defendantSolicitorParty.key}`,
      },
    },
  };
};

const statementOfTruth = (defendantSolicitorParty: Party) => {
  const defendantSolicitorUser =
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
      ? defendantSolicitor2User
      : defendantSolicitor1User;

  return {
    StatementOfTruth: {
      uiStatementOfTruth: {
        name: defendantSolicitorUser.name,
        role: 'Solicitor',
      },
    },
  };
};

const undefine = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      Undefine: {
        respondent1DetailsForClaimDetailsTab: undefined,
      },
    };
  } else {
    return {};
  }
};

const defendantResponseDataComponents = {
  confirmDetails,
  singleResponse,
  respondentResponseType,
  solicitorReferences,
  upload,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocuments,
  disclosureOfNonElectronicDocuments,
  deterWithoutHearing,
  experts,
  witnesses,
  language,
  hearing,
  draftDirections,
  requestedCourt,
  hearingSupport,
  vulnerabilityQuestions,
  furtherInformation,
  statementOfTruth,
  undefine,
};

export default defendantResponseDataComponents;
