import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import partys from '../../../../../constants/users/partys';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import DateHelper from '../../../../../helpers/date-helper';
import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';

const respondentResponse = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2Represented(claimType)) {
    return {
      RespondentResponse: {
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2: 'Yes',
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2: 'Yes',
      },
    };
  } else if (claimType === ClaimType.TWO_VS_ONE) {
    return {
      RespondentResponse: {
        applicant1ProceedWithClaimMultiParty2v1: 'Yes',
        applicant2ProceedWithClaimMultiParty2v1: 'Yes',
      },
    };
  }

  return {
    RespondentResponse: {
      applicant1ProceedWithClaim: 'Yes',
    },
  };
};

const applicantDefenceResponseDocument = (
  claimType: ClaimType,
  defenceResponseDocument1: UploadDocumentValue,
  defenceResponseDocument2: UploadDocumentValue,
) => {
  if (claimType === ClaimType.ONE_VS_TWO_DIFF_SOL) {
    return {
      ApplicantDefenceResponseDocument: {
        applicant1DefenceResponseDocument: { file: defenceResponseDocument1 },
        claimantDefenceResDocToDefendant2: { file: defenceResponseDocument2 },
      },
    };
  }

  return {
    ApplicantDefenceResponseDocument: {
      applicant1DefenceResponseDocument: {
        file: defenceResponseDocument1,
      },
    },
  };
};

const deterWithHearing = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.SMALL_CLAIM) {
    return {
      DeterminationWithoutHearing: {
        deterWithoutHearing: {
          deterWithoutHearingWhyNot: `Determination without hearing reason - ${partys.CLAIMANT_1.key}`,
          deterWithoutHearingYesNo: 'No',
        },
      },
    };
  }

  return {};
};

const fileDirectionsQuestionnaire = (claimTrack: ClaimTrack) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM
  ) {
    return {
      FileDirectionsQuestionnaire: {
        applicant1DQFileDirectionsQuestionnaire: {
          explainedToClient: ['CONFIRM'],
          oneMonthStayRequested: claimTrack === ClaimTrack.MULTI_CLAIM ? 'No' : 'Yes',
          reactionProtocolCompliedWith: 'No',
          reactionProtocolNotCompliedWithReason:
            claimTrack === ClaimTrack.MULTI_CLAIM
              ? `No explanation - ${partys.CLAIMANT_1.key}`
              : `Reaction protocol not complied reason - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }

  return {};
};

const fixedRecoverableCosts = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM) {
    return {
      FixedRecoverableCosts: {
        applicant1DQFixedRecoverableCosts: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_1',
          complexityBandingAgreed: 'Yes',
          reasons: `Recoverable costs reason - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }

  return {};
};

const fixedRecoverableCostsIntermediate = (
  claimTrack: ClaimTrack,
  frcSupportingDocument?: UploadDocumentValue,
) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      FixedRecoverableCostsIntermediate: {
        applicant1DQFixedRecoverableCostsIntermediate: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_1',
          complexityBandingAgreed: 'Yes',
          reasons: `Recoverable costs reason - ${partys.CLAIMANT_1.key}`,
          frcSupportingDocument,
        },
      },
    };
  }

  return {};
};

const disclosureOfElectronicDocuments = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      DisclosureOfElectronicDocuments: {
        applicant1DQDisclosureOfElectronicDocuments: {
          reachedAgreement: 'No',
          agreementLikely: 'Yes',
        },
      },
    };
  }

  return {};
};

const disclosureOfNonElectronicDocuments = (claimTrack: ClaimTrack) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM
  ) {
    return {
      DisclosureOfNonElectronicDocuments: {
        applicant1DQDisclosureOfNonElectronicDocuments: {
          directionsForDisclosureProposed: 'Yes',
          standardDirectionsRequired: 'No',
          bespokeDirections:
            claimTrack === ClaimTrack.MULTI_CLAIM
              ? `No directions required - ${partys.CLAIMANT_1.key}`
              : `Directions are proposed for disclosure - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }

  return {};
};

const disclosureReport = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      DisclosureReport: {
        applicant1DQDisclosureReport: {
          disclosureFormFiledAndServed: 'Yes',
          disclosureProposalAgreed: 'Yes',
          draftOrderNumber: '012345',
        },
      },
    };
  }

  return {};
};

const experts = {
  Experts: {
    applicant1DQExperts: {
      expertRequired: 'Yes',
      expertReportsSent: 'NOT_OBTAINED',
      jointExpertSuitable: 'Yes',
      details: [
        CaseDataHelper.setIdToData({
          ...CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_1),
          partyName: undefined,
        }),
        CaseDataHelper.setIdToData({
          ...CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_2),
          partyName: undefined,
        }),
      ],
    },
  },
};

const witnesses = {
  Witnesses: {
    applicant1DQWitnesses: {
      witnessesToAppear: 'Yes',
      details: [
        CaseDataHelper.setIdToData({
          ...CaseDataHelper.buildWitnessData(partys.CLAIMANT_WITNESS_1),
          partyName: undefined,
        }),
        CaseDataHelper.setIdToData({
          ...CaseDataHelper.buildWitnessData(partys.CLAIMANT_WITNESS_2),
          partyName: undefined,
        }),
      ],
    },
  },
};

const language = {
  Language: {
    applicant1DQLanguage: {
      court: 'BOTH',
      documents: 'BOTH',
    },
  },
};

const hearing = {
  Hearing: {
    applicant1DQHearing: {
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

const draftDirections = (draftDirectionsDocument: UploadDocumentValue) => ({
  DraftDirections: {
    applicant1DQDraftDirections: draftDirectionsDocument,
  },
});

const hearingSupport = {
  HearingSupport: {
    applicant1DQHearingSupport: {
      supportRequirements: 'Yes',
      supportRequirementsAdditional: `Support requirements for ${partys.CLAIMANT_1.key}`,
    },
  },
};

const vulnerabilityQuestions = {
  VulnerabilityQuestions: {
    applicant1DQVulnerabilityQuestions: {
      vulnerabilityAdjustmentsRequired: 'Yes',
      vulnerabilityAdjustments: `Vulnerability adjustments - ${partys.CLAIMANT_1.key}`,
    },
  },
};

const furtherInformation = {
  FurtherInformation: {
    applicant1DQFurtherInformation: {
      futureApplications: 'Yes',
      reasonForFutureApplications: `Further information - ${partys.CLAIMANT_1.key}`,
      otherInformationForJudge: `Further information - ${partys.CLAIMANT_1.key}`,
    },
  },
};

const statementOfTruth = {
  StatementOfTruth: {
    uiStatementOfTruth: {
      name: claimantSolicitorUser.name,
      role: 'Solicitor',
    },
  },
};

const claimantResponseDataComponents = {
  respondentResponse,
  applicantDefenceResponseDocument,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocuments,
  disclosureOfNonElectronicDocuments,
  disclosureReport,
  deterWithHearing,
  experts,
  witnesses,
  language,
  hearing,
  draftDirections,
  hearingSupport,
  vulnerabilityQuestions,
  furtherInformation,
  statementOfTruth,
};

export default claimantResponseDataComponents;
