import { z } from 'zod';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import DefendantResponseType from '../../../../../constants/ccd-events/defendant-response/unspec/defendant-response-type';
import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';

const yesNoSchema = z.enum(['Yes', 'No']);
const nonEmptyString = z.string().min(1);

const confirmDetails = {};

const singleResponse = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      respondentResponseIsSame: z.literal('Yes'),
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
        respondent1ClaimResponseType: z.literal(responseType),
      };
    } else if (claimType === ClaimType.TWO_VS_ONE) {
      return {
        respondent1ClaimResponseType: z.literal(responseType),
        respondent1ClaimResponseTypeToApplicant2: z.literal(responseType),
      };
    }

    return {
      respondent1ClaimResponseType: z.literal(responseType),
      multiPartyResponseTypeFlags: z.literal(responseType),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2ClaimResponseType: z.literal(responseType),
    };
  }

  return {};
};

const solicitorReferences = {};

const upload = {};

const deterWithoutHearing = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (claimTrack === ClaimTrack.SMALL_CLAIM) {
    if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
      return {
        deterWithoutHearingRespondent1: z
          .strictObject({
            deterWithoutHearingWhyNot: nonEmptyString,
            deterWithoutHearingYesNo: yesNoSchema,
          })
          .optional(),
      };
    } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
      return {
        deterWithoutHearingRespondent2: z
          .strictObject({
            deterWithoutHearingWhyNot: nonEmptyString,
            deterWithoutHearingYesNo: yesNoSchema,
          })
          .optional(),
      };
    }
  }

  return {};
};

const fileDirectionsQuestionnaire = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (
    claimTrack === ClaimTrack.FAST_CLAIM ||
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
    claimTrack === ClaimTrack.MULTI_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQFileDirectionsQuestionnaire'
        : 'respondent1DQFileDirectionsQuestionnaire']: z.strictObject({
        explainedToClient: z.array(nonEmptyString).min(1),
        oneMonthStayRequested: yesNoSchema,
        reactionProtocolCompliedWith: yesNoSchema,
        reactionProtocolNotCompliedWithReason: nonEmptyString,
      }),
    };
  }

  return {};
};

const fixedRecoverableCosts = (claimTrack: ClaimTrack, defendantSolicitorParty: Party) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQFixedRecoverableCosts'
        : 'respondent1DQFixedRecoverableCosts']: z.strictObject({
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
  defendantSolicitorParty: Party,
) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQFixedRecoverableCostsIntermediate'
        : 'respondent1DQFixedRecoverableCostsIntermediate']: z.strictObject({
        isSubjectToFixedRecoverableCostRegime: yesNoSchema,
        band: nonEmptyString,
        complexityBandingAgreed: yesNoSchema,
        reasons: nonEmptyString,
        frcSupportingDocument: z.looseObject({}),
      }),
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
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQDisclosureOfElectronicDocuments'
        : 'respondent1DQDisclosureOfElectronicDocuments']: z.strictObject({
        reachedAgreement: yesNoSchema,
        agreementLikely: yesNoSchema,
      }),
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
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
        ? 'respondent2DQDisclosureOfNonElectronicDocuments'
        : 'respondent1DQDisclosureOfNonElectronicDocuments']: z.strictObject({
        directionsForDisclosureProposed: yesNoSchema,
        standardDirectionsRequired: yesNoSchema,
        bespokeDirections: nonEmptyString,
      }),
    };
  }

  return {};
};

const experts = (defendantSolicitorParty: Party) => ({
  [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
    ? 'respondent2DQExperts'
    : 'respondent1DQExperts']: z.strictObject({
    expertRequired: yesNoSchema,
    expertReportsSent: nonEmptyString,
    jointExpertSuitable: yesNoSchema,
    details: z
      .array(
        z.strictObject({
          id: nonEmptyString,
          value: z.looseObject({
            partyID: nonEmptyString,
            firstName: nonEmptyString,
            lastName: nonEmptyString,
            phoneNumber: nonEmptyString,
            emailAddress: nonEmptyString,
            fieldOfExpertise: nonEmptyString,
            whyRequired: nonEmptyString,
            estimatedCost: nonEmptyString,
            eventAdded: nonEmptyString,
            dateAdded: nonEmptyString,
          }),
        }),
      )
      .min(1),
  }),
});

const witnesses = (defendantSolicitorParty: Party) => ({
  [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2
    ? 'respondent2DQWitnesses'
    : 'respondent1DQWitnesses']: z.strictObject({
    witnessesToAppear: yesNoSchema,
    details: z
      .array(
        z.strictObject({
          id: nonEmptyString,
          value: z.looseObject({
            partyID: nonEmptyString,
            firstName: nonEmptyString,
            lastName: nonEmptyString,
            phoneNumber: nonEmptyString,
            emailAddress: nonEmptyString,
            reasonForWitness: nonEmptyString,
            eventAdded: nonEmptyString,
            dateAdded: nonEmptyString,
          }),
        }),
      )
      .min(1),
  }),
});

const language = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQLanguage: z.strictObject({
        court: nonEmptyString,
        documents: nonEmptyString,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQLanguage: z.strictObject({
        court: nonEmptyString,
        documents: nonEmptyString,
      }),
    };
  }

  return {};
};

const hearing = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQHearing: z.looseObject({
        unavailableDatesRequired: yesNoSchema,
        unavailableDates: z.array(z.looseObject({})).min(1),
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQHearing: z.looseObject({
        unavailableDatesRequired: yesNoSchema,
        unavailableDates: z.array(z.looseObject({})).min(1),
      }),
    };
  }

  return {};
};

const draftDirections = {};

const requestedCourt = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQRequestedCourt: z.looseObject({
        reasonForHearingAtSpecificCourt: nonEmptyString,
      }),
      respondent1DQRemoteHearing: z.looseObject({
        remoteHearingRequested: yesNoSchema,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQRequestedCourt: z.looseObject({
        reasonForHearingAtSpecificCourt: nonEmptyString,
      }),
      respondent2DQRemoteHearing: z.looseObject({
        remoteHearingRequested: yesNoSchema,
      }),
    };
  }

  return {};
};

const hearingSupport = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQHearingSupport: z.strictObject({
        supportRequirements: yesNoSchema,
        supportRequirementsAdditional: nonEmptyString,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQHearingSupport: z.strictObject({
        supportRequirements: yesNoSchema,
        supportRequirementsAdditional: nonEmptyString,
      }),
    };
  }

  return {};
};

const vulnerabilityQuestions = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQVulnerabilityQuestions: z.strictObject({
        vulnerabilityAdjustmentsRequired: yesNoSchema,
        vulnerabilityAdjustments: nonEmptyString,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQVulnerabilityQuestions: z.strictObject({
        vulnerabilityAdjustmentsRequired: yesNoSchema,
        vulnerabilityAdjustments: nonEmptyString,
      }),
    };
  }

  return {};
};

const furtherInformation = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQFurtherInformation: z.strictObject({
        futureApplications: yesNoSchema,
        reasonForFutureApplications: nonEmptyString,
        otherInformationForJudge: nonEmptyString,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQFurtherInformation: z.strictObject({
        futureApplications: yesNoSchema,
        reasonForFutureApplications: nonEmptyString,
        otherInformationForJudge: nonEmptyString,
      }),
    };
  }

  return {};
};

const statementOfTruth = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1DQStatementOfTruth: z.strictObject({
        name: nonEmptyString,
        role: nonEmptyString,
      }),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2DQStatementOfTruth: z.strictObject({
        name: nonEmptyString,
        role: nonEmptyString,
      }),
    };
  }

  return {};
};

const undefine = (defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      isRespondent1: z.undefined().optional(),
      respondent2DocumentGeneration: z.undefined().optional(),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent1ClaimResponseDocument: z.undefined().optional(),
      respondent1DQDraftDirections: z.undefined().optional(),
    };
  }
};

const defendantResponseSchemaComponents = {
  confirmDetails,
  singleResponse,
  solicitorReferences,
  respondentResponseType,
  upload,
  deterWithoutHearing,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocuments,
  disclosureOfNonElectronicDocuments,
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

export default defendantResponseSchemaComponents;
