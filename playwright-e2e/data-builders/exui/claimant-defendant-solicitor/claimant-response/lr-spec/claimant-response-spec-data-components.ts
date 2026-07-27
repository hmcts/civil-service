import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import preferredCourts from '../../../../../config/preferred-courts';
import partys from '../../../../../constants/users/partys';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import { UploadDocumentValue } from '../../../../../models/ccd-case-data';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import ClaimantResponseSpecType from '../../../../../constants/ccd-events/claimant-response-spec-type/claimant-response-spec-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import DateHelper from '../../../../../helpers/date-helper';

const defendantResponse = (
  claimType: ClaimType,
  claimantResponseSpecType: ClaimantResponseSpecType,
) => {
  if (
    claimantResponseSpecType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE
  ) {
    if (ClaimTypeHelper.isClaimant2(claimType)) {
      return {
        RespondentResponse: {
          applicant1ProceedWithClaimSpec2v1:
            claimantResponseSpecType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ? 'Yes' : 'No',
        },
      };
    }

    return {
      RespondentResponse: {
        applicant1ProceedWithClaim:
          claimantResponseSpecType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ? 'Yes' : 'No',
      },
    };
  } else if (
    claimantResponseSpecType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseSpecType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID ||
    claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT_PAID_CONFIRM_HAS_PAID
  ) {
    return {
      RespondentResponse: {
        applicant1PartAdmitConfirmAmountPaidSpec:
          claimantResponseSpecType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID
          ? 'No' : 'Yes'
      }
    };
  } else if (
    claimantResponseSpecType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT
  ) {
    return {
      RespondentResponse: {
        applicant1AcceptAdmitAmountPaidSpec: claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT
          ? 'Yes'
          : 'No',
      },
    };
  }

  return {};
};

const intentionToSettle = (claimantResponseSpecType: ClaimantResponseSpecType) => {
  if (
    claimantResponseSpecType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID ||
    claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT_PAID_CONFIRM_HAS_PAID
  ) {
    return {
      IntentionToSettleClaim: {
        applicant1PartAdmitIntentionToSettleClaimSpec:
          claimantResponseSpecType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT_PAID_CONFIRM_HAS_PAID
          ? 'Yes' : 'No',
      }
    };
  }

  return {};
};


const claimantDefenceResponseDocument = (
  defenceResponseDocumentSpec: UploadDocumentValue | undefined,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID
  )
    return {
      ApplicantDefenceResponseDocument: {
        applicant1DefenceResponseDocumentSpec: {
          file: defenceResponseDocumentSpec,
        },
      },
    };

  return {};
};

const mediationContactInformation = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimTrack === ClaimTrack.SMALL_CLAIM &&
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
  )
    return {
      MediationContactInformation: {
        app1MediationContactInfo: CaseDataHelper.buildMediationData(
          partys.CLAIMANT_1_MEDIATION_FRIEND,
        ),
      },
    };

  return {};
};

const mediationAvailability = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimTrack === ClaimTrack.SMALL_CLAIM &&
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
  ) {
    const fromDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1 }), {
      outputFormat: 'YYYY-MM-DD',
    });
    const toDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1, days: 3 }), {
      outputFormat: 'YYYY-MM-DD',
    });

    return {
      MediationAvailability: {
        app1MediationAvailability: {
          isMediationUnavailablityExists: 'Yes',
          unavailableDatesForMediation: [
            CaseDataHelper.setIdToData({ fromDate, toDate, unavailableDateType: 'DATE_RANGE' }),
          ],
        },
      },
    };
  }

  return {};
};

const fileDirectionsQuestionnaire = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&

    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      FileDirectionsQuestionnaire: {
        applicant1DQFileDirectionsQuestionnaire: {
          oneMonthStayRequested: 'Yes',
          reactionProtocolCompliedWith: 'No',
          reactionProtocolNotCompliedWithReason: `Reaction protocol not complied with reason - ${partys.CLAIMANT_1.key}`,
          explainedToClient: ['CONFIRM'],
        },
      },
    };
  }

  return {};
};

const fixedRecoverableCosts = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    claimTrack === ClaimTrack.FAST_CLAIM
  ) {
    return {
      FixedRecoverableCosts: {
        applicant1DQFixedRecoverableCosts: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_4',
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
  claimantResponseType: ClaimantResponseSpecType,
  frcSupportingDocument?: UploadDocumentValue,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM
  ) {
    return {
      FixedRecoverableCostsIntermediate: {
        applicant1DQFixedRecoverableCostsIntermediate: {
          isSubjectToFixedRecoverableCostRegime: 'Yes',
          band: 'BAND_2',
          complexityBandingAgreed: 'Yes',
          reasons: `Recoverable costs reason - ${partys.CLAIMANT_1.key}`,
          frcSupportingDocument,
        },
      },
    };
  }

  return {};
};

const disclosureOfElectronicDocuments = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      DisclosureOfElectronicDocuments: {
        specApplicant1DQDisclosureOfElectronicDocuments: {
          reachedAgreement: 'No',
          agreementLikely: 'No',
          reasonForNoAgreement: `Reason for no agreement - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }

  return {};
};

const disclosureOfNonElectronicDocuments = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      DisclosureOfNonElectronicDocuments: {
        specApplicant1DQDisclosureOfNonElectronicDocuments: {
          bespokeDirections: `Directions are proposed for disclosure - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }

  return {};
};

const disclosureReport = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      DisclosureReport: {
        applicant1DQDisclosureReport: {
          disclosureFormFiledAndServed: 'Yes',
          disclosureProposalAgreed: 'Yes',
          draftOrderNumber: '12345',
        },
      },
    };
  }

  return {};
};

const experts = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        SmallClaimExperts: {
          applicant1RespondToClaimExperts: {
            ...CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_1),
            partyName: undefined,
          },
          applicant1ClaimExpertSpecRequired: 'Yes',
        },
      };

    return {
      Experts: {
        applicant1DQExperts: {
          expertRequired: 'Yes',
          expertReportsSent: 'YES',
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
  }
  return {};
};

const determinationWithoutHearing = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimTrack === ClaimTrack.SMALL_CLAIM &&
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
  ) {
    return {
      DeterminationWithoutHearing: {
        deterWithoutHearing: {
          deterWithoutHearingYesNo: 'No',
          deterWithoutHearingWhyNot: `Determination without hearing reason - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }
  return {};
};

const witnesses = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        SmallClaimWitnesses: {
          applicant1DQWitnessesSmallClaim: {
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

    return {
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
  }

  return {};
};

const language = (
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    return {
      Language: {
        applicant1DQLanguage: {
          court: 'ENGLISH',
          documents: 'ENGLISH',
        },
      },
    };
  }

  return {};
};

const hearing = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    const fromDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1 }), {
      outputFormat: 'YYYY-MM-DD',
    });
    const toDate = DateHelper.formatDateToString(DateHelper.addToToday({ months: 1, days: 3 }), {
      outputFormat: 'YYYY-MM-DD',
    });

    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        Hearing: {
          applicant1DQSmallClaimHearing: {
            unavailableDatesRequired: 'Yes',
            smallClaimUnavailableDate: [
              CaseDataHelper.setIdToData({ fromDate, toDate, unavailableDateType: 'DATE_RANGE' }),
            ],
          },
        },
      };
  }
  return {};
};

const requestedCourtLocation = (
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    const preferredCourt = CaseDataHelper.setCodeToData(
      preferredCourts[partys.CLAIMANT_1.key].default,
    );

    return {
      ApplicantCourtLocationLRspec: {
        applicant1DQRequestedCourt: {
          responseCourtLocations: {
            list_items: [preferredCourt],
            value: preferredCourt,
          },
          reasonForHearingAtSpecificCourt: `Court location reason - ${partys.CLAIMANT_1.key}`,
        },
        applicant1DQRemoteHearingLRspec: {
          remoteHearingRequested: 'Yes',
          reasonForRemoteHearing: `Remote hearing reason - ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }
  return {};
};

const hearingSupport = (
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
    return {
      HearingSupport: {
        applicant1DQHearingSupport: {
          supportRequirements: 'Yes',
          supportRequirementsAdditional: `Support requirements for ${partys.CLAIMANT_1.key}`,
        },
      },
    };
  }
  return {};
};

const vulnerabilityQuestions = (claimantResponseType: ClaimantResponseSpecType) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) {
      return {
        VulnerabilityQuestions: {
          applicant1DQVulnerabilityQuestions: {
            vulnerabilityAdjustmentsRequired: 'Yes',
            vulnerabilityAdjustments: `Vulnerability adjustments - ${partys.CLAIMANT_1.key}`,
          },
        },
      };
    }
  return {};
};

const application = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM &&
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
      claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
  )
    return {
      Application: {
        applicant1DQFutureApplications: {
          intentionToMakeFutureApplications: 'Yes',
          whatWillFutureApplicationsBeMadeFor: `Future applications will be made for - ${partys.CLAIMANT_1.key}`,
        },
      },
    };

  return {};
};

const statementOfTruth = {
  StatementOfTruth: {
    uiStatementOfTruth: {
      name: claimantSolicitorUser.name,
      role: 'Solicitor',
    },
  },
};

const undefine = (claimantResponseType: ClaimantResponseSpecType,) => {
  if (claimantResponseType === ClaimantResponseSpecType.ACCEPT_FULL_ADMIT) {
    return {
      Undefine: {
        respondToCourtLocation: undefined,
        respondToCourtLocation2: undefined,
        applicant1DQRequestedCourt: undefined,
      },
    };
  }

  return {};
};

const claimantResponseSpecData = {
  undefine,
  defendantResponse,
  intentionToSettle,
  claimantDefenceResponseDocument,
  mediationContactInformation,
  mediationAvailability,
  determinationWithoutHearing,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocuments,
  disclosureOfNonElectronicDocuments,
  disclosureReport,
  experts,
  witnesses,
  language,
  hearing,
  requestedCourtLocation,
  hearingSupport,
  vulnerabilityQuestions,
  application,
  statementOfTruth,
};

export default claimantResponseSpecData;
