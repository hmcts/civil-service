import { z } from 'zod';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';

const yesNoSchema = z.enum(['Yes', 'No']);
const nonEmptyString = z.string().min(1);

const respondentResponse = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2Represented(claimType)) {
    return {
      applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2: yesNoSchema,
      applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2: yesNoSchema,
      claimant2ResponseFlag: yesNoSchema,
      applicantsProceedIntention: yesNoSchema,
      claimantResponseDocumentToDefendant2Flag: yesNoSchema,
    };
  } else if (claimType === ClaimType.TWO_VS_ONE) {
    return {
      applicant1ProceedWithClaimMultiParty2v1: yesNoSchema,
      applicant2ProceedWithClaimMultiParty2v1: yesNoSchema,
    };
  }

  return {
    applicant1ProceedWithClaim: yesNoSchema,
  };
};

const applicantDefenceResponseDocument = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_DIFF_SOL) {
    return {};
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

const fixedRecoverableCosts = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.FAST_CLAIM) {
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

const fixedRecoverableCostsIntermediate = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
    return {
      applicant1DQFixedRecoverableCostsIntermediate: z.strictObject({
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

const disclosureOfElectronicDocuments = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM || claimTrack === ClaimTrack.MULTI_CLAIM) {
    return {
      applicant1DQDisclosureOfElectronicDocuments: z.strictObject({
        reachedAgreement: yesNoSchema,
        agreementLikely: yesNoSchema,
      }),
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
      applicant1DQDisclosureOfNonElectronicDocuments: z.strictObject({
        directionsForDisclosureProposed: yesNoSchema,
        standardDirectionsRequired: yesNoSchema,
        bespokeDirections: nonEmptyString,
      }),
    };
  }

  return {};
};

const disclosureReport = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.INTERMEDIATE_CLAIM) {
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

const deterWithoutHearing = (claimTrack: ClaimTrack) => {
  if (claimTrack === ClaimTrack.SMALL_CLAIM) {
    return {
      deterWithoutHearing: z
        .strictObject({
          deterWithoutHearingWhyNot: nonEmptyString,
          deterWithoutHearingYesNo: yesNoSchema,
        })
        .optional(),
    };
  }

  return {};
};

const experts = {
  applicant1DQExperts: z.strictObject({
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
};

const witnesses = {
  applicant1DQWitnesses: z.strictObject({
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
};

const language = {
  applicant1DQLanguage: z.strictObject({
    court: nonEmptyString,
    documents: nonEmptyString,
  }),
};

const hearing = {
  applicant1DQHearing: z.looseObject({
    unavailableDatesRequired: yesNoSchema,
    unavailableDates: z.array(z.looseObject({})).min(1),
  }),
};

const draftDirections = {};

const hearingSupport = {
  applicant1DQHearingSupport: z.strictObject({
    supportRequirements: yesNoSchema,
    supportRequirementsAdditional: nonEmptyString,
  }),
};

const vulnerabilityQuestions = {
  applicant1DQVulnerabilityQuestions: z.strictObject({
    vulnerabilityAdjustmentsRequired: yesNoSchema,
    vulnerabilityAdjustments: nonEmptyString,
  }),
};

const furtherInformation = {
  applicant1DQFurtherInformation: z.strictObject({
    futureApplications: yesNoSchema,
    reasonForFutureApplications: nonEmptyString,
    otherInformationForJudge: nonEmptyString,
  }),
};

const undefine = {
  nextDeadline: z.undefined().optional(),
};

const claimantResponseSchemaComponents = {
  respondentResponse,
  applicantDefenceResponseDocument,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocuments,
  disclosureOfNonElectronicDocuments,
  disclosureReport,
  deterWithoutHearing,
  experts,
  witnesses,
  language,
  hearing,
  draftDirections,
  hearingSupport,
  vulnerabilityQuestions,
  furtherInformation,
  undefine,
};

export default claimantResponseSchemaComponents;
