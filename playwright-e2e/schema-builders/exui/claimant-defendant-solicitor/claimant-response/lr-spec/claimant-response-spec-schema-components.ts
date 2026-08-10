import { z } from 'zod';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimantResponseSpecType from '../../../../../constants/ccd-events/claimant-response-spec-type/claimant-response-spec-type';

const yesNoSchema = z.enum(['Yes', 'No']);
const nonEmptyString = z.string().min(1);

const defendantResponse = (
  claimType: ClaimType,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.ACCEPT_FULL_DEFENCE
  ) {
    if (ClaimTypeHelper.isClaimant2(claimType)) {
      return {
        applicant1ProceedWithClaimSpec2v1: yesNoSchema,
      };
    }

    return {
      applicant1ProceedWithClaim: yesNoSchema,
    };
  }

  if (
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID ||
    claimantResponseType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT_PAID_CONFIRM_HAS_PAID
  ) {
    return {
      applicant1PartAdmitConfirmAmountPaidSpec: yesNoSchema,
    };
  }

  if (
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT
  ) {
    return {
      applicant1AcceptAdmitAmountPaidSpec: yesNoSchema,
    };
  }

  return {};
};

const intentionToSettle = (
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID ||
    claimantResponseType === ClaimantResponseSpecType.ACCEPT_PART_ADMIT_PAID_CONFIRM_HAS_PAID
  ) {
    return {
      applicant1PartAdmitIntentionToSettleClaimSpec: yesNoSchema,
    };
  }

  return {};
};

const claimantDefenceResponseDocument = (
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID)
    return {
      applicant1DefenceResponseDocumentSpec: z.looseObject({}),
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
      app1MediationContactInfo: z.looseObject({}),
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
  )
    return {
      app1MediationAvailability: z.looseObject({
        isMediationUnavailablityExists: yesNoSchema,
        unavailableDatesForMediation: z.array(z.looseObject({})).min(1),
      }),
    };

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
      // deterWithoutHearing: z.looseObject({
      //   deterWithoutHearingYesNo: yesNoSchema,
      //   deterWithoutHearingWhyNot: nonEmptyString,
      // }),
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
      applicant1DQFileDirectionsQuestionnaire: z.strictObject({
        explainedToClient: z.array(nonEmptyString).min(1),
        oneMonthStayRequested: yesNoSchema,
        reactionProtocolCompliedWith: yesNoSchema,
        reactionProtocolNotCompliedWithReason: nonEmptyString,
      }),
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
      applicant1DQFixedRecoverableCosts: z.strictObject({
        isSubjectToFixedRecoverableCostRegime: yesNoSchema,
        band: nonEmptyString,
        complexityBandingAgreed: yesNoSchema,
        reasons: nonEmptyString,
      }),
    };
  }

  return {};
};

const fixedRecoverableCostsIntermediate = (
  claimTrack: ClaimTrack,
  claimantResponseType: ClaimantResponseSpecType,
) => {
  if (
    (claimantResponseType === ClaimantResponseSpecType.REJECT_FULL_DEFENCE ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_NOT_PAID ||
    claimantResponseType === ClaimantResponseSpecType.REJECT_PART_ADMIT_PAID_CONFIRM_PAID) &&
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM
  ) {
    return {
      applicant1DQFixedRecoverableCostsIntermediate: z.strictObject({
        isSubjectToFixedRecoverableCostRegime: yesNoSchema,
        band: nonEmptyString,
        complexityBandingAgreed: yesNoSchema,
        reasons: nonEmptyString,
        frcSupportingDocument: z.looseObject({
          document_url: nonEmptyString,
          document_binary_url: nonEmptyString,
          document_filename: nonEmptyString,
        }),
      }),
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
      specApplicant1DQDisclosureOfElectronicDocuments: z.strictObject({
        reachedAgreement: yesNoSchema,
        agreementLikely: yesNoSchema,
        reasonForNoAgreement: nonEmptyString,
      }),
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
      specApplicant1DQDisclosureOfNonElectronicDocuments: z.strictObject({
        bespokeDirections: nonEmptyString,
      }),
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
      applicant1DQDisclosureReport: z.strictObject({
        disclosureFormFiledAndServed: yesNoSchema,
        disclosureProposalAgreed: yesNoSchema,
        draftOrderNumber: nonEmptyString,
      }),
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
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        applicant1RespondToClaimExperts: z.looseObject({}),
        applicant1ClaimExpertSpecRequired: yesNoSchema,
      };
    }

    return {
      applicant1DQExperts: z.looseObject({
        // expertRequired: yesNoSchema,
        expertReportsSent: nonEmptyString,
        jointExpertSuitable: yesNoSchema,
        details: z
          .array(
            z.strictObject({
              id: nonEmptyString,
              value: z.looseObject({
                firstName: nonEmptyString,
                lastName: nonEmptyString,
                emailAddress: nonEmptyString,
                phoneNumber: nonEmptyString,
                fieldOfExpertise: nonEmptyString,
                whyRequired: nonEmptyString,
                estimatedCost: nonEmptyString,
              }),
            }),
          )
          .min(1),
      }),
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
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        applicant1DQWitnessesSmallClaim: z.strictObject({
          details: z
            .array(
              z.strictObject({
                id: nonEmptyString,
                value: z.looseObject({
                  firstName: nonEmptyString,
                  lastName: nonEmptyString,
                  phoneNumber: nonEmptyString,
                  emailAddress: nonEmptyString,
                  reasonForWitness: nonEmptyString,
                }),
              }),
            )
            .min(1),
          witnessesToAppear: yesNoSchema,
        }),
      };
    }

    return {
      applicant1DQWitnesses: z.strictObject({
        details: z
          .array(
            z.strictObject({
              id: nonEmptyString,
              value: z.looseObject({
                firstName: nonEmptyString,
                lastName: nonEmptyString,
                phoneNumber: nonEmptyString,
                emailAddress: nonEmptyString,
                reasonForWitness: nonEmptyString,
              }),
            }),
          )
          .min(1),
        witnessesToAppear: yesNoSchema,
      }),
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
      applicant1DQLanguage: z.strictObject({
        court: nonEmptyString,
        documents: nonEmptyString,
      }),
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
    if (claimTrack === ClaimTrack.SMALL_CLAIM) {
      return {
        applicant1DQSmallClaimHearing: z.looseObject({
          unavailableDatesRequired: yesNoSchema,
          smallClaimUnavailableDate: z.array(z.looseObject({})).min(1),
        }),
      };
    }

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
    return {
      applicant1DQRequestedCourt: z.looseObject({}),
      applicant1DQRemoteHearingLRspec: z.looseObject({
        remoteHearingRequested: yesNoSchema,
        reasonForRemoteHearing: nonEmptyString,
      }),
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
      applicant1DQHearingSupport: z.strictObject({
        supportRequirements: yesNoSchema,
        supportRequirementsAdditional: nonEmptyString,
      }),
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
      applicant1DQVulnerabilityQuestions: z.strictObject({
        vulnerabilityAdjustmentsRequired: yesNoSchema,
        vulnerabilityAdjustments: nonEmptyString,
      }),
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
  ) {
    return {
      applicant1DQFutureApplications: z.strictObject({
        intentionToMakeFutureApplications: yesNoSchema,
        whatWillFutureApplicationsBeMadeFor: nonEmptyString,
      }),
    };
  }

  return {};
};

const undefine = {
  nextDeadline: z.undefined().optional(),
};

const claimantResponseSpecSchemaComponents = {
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
};

export default claimantResponseSpecSchemaComponents;
