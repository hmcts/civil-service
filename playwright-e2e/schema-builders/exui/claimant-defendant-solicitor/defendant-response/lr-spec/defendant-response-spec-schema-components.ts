import { z } from 'zod';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import DefendantResponseSpecType from '../../../../../constants/ccd-events/defendant-response/lr-spec/defendant-response-spec-type';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import partys from '../../../../../constants/users/partys';
import { Party } from '../../../../../models/users/partys';
import PaymentTypeSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/payment-type-spec';
import DefenceRouteSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/defence-route-spec';
import DefenceAdmittedPartRouteSpec from '../../../../../constants/ccd-events/defendant-response/lr-spec/defence-admitted-part-route-spec';

const yesNoSchema = z.enum(['Yes', 'No']);
const nonEmptyString = z.string().min(1);

const responseClaimTrack = (responseType: DefendantResponseSpecType) => {
  if (responseType !== DefendantResponseSpecType.COUNTER_CLAIM) {
    return {
      responseClaimTrack: nonEmptyString,
    };
  }

  return {};
};

const responseConfirmNameAddress = (claimType: ClaimType, defendantSolicitorParty: Party) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      specAoSApplicantCorrespondenceAddressRequired: yesNoSchema,
      ...(claimType === ClaimType.ONE_VS_TWO_SAME_SOL
        ? { specAoSRespondent2HomeAddressRequired: yesNoSchema }
        : {}),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      specAoSRespondent2HomeAddressRequired: yesNoSchema,
    };
  }

  return {};
};

const responseConfirmDetails = (defendantSolicitorParty: Party) => ({
  [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
    ? 'specAoSRespondentCorrespondenceAddressRequired'
    : 'specAoSRespondent2CorrespondenceAddressRequired']: yesNoSchema,
});

const singleResponse = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL)
    return {
      respondentResponseIsSame: yesNoSchema,
    };

  if (ClaimTypeHelper.isClaimant2(claimType)) {
    return {
      defendantSingleResponseToBothClaimants: yesNoSchema,
    };
  }

  return {};
};

const respondentResponseType = (
  responseType: DefendantResponseSpecType,
  claimType: ClaimType,
  defendantSolicitorParty: Party,
) => {
  if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
    return {
      respondent1ClaimResponseTypeForSpec: z.literal(responseType),
      ...(ClaimTypeHelper.isClaimant2(claimType)
        ? {
            claimant1ClaimResponseTypeForSpec: z.literal(responseType),
            claimant2ClaimResponseTypeForSpec: z.literal(responseType),
          }
        : {}),
    };
  } else if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2) {
    return {
      respondent2ClaimResponseTypeForSpec: z.literal(responseType),
    };
  }

  return {};
};

const defenceRoute = (
  responseType: DefendantResponseSpecType,
  defenceRouteSpec: DefenceRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (responseType === DefendantResponseSpecType.FULL_DEFENCE) {
    if (defenceRouteSpec === DefenceRouteSpec.DISPUTE) {
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'defenceRouteRequired'
          : 'defenceRouteRequired2']: z.literal(defenceRouteSpec),
      };
    }

    if (defenceRouteSpec === DefenceRouteSpec.HAS_PAID) {
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'defenceRouteRequired'
          : 'defenceRouteRequired2']: z.literal(defenceRouteSpec),
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToClaim'
          : 'respondToClaim2']: z.looseObject({
          howMuchWasPaid: nonEmptyString,
          howWasThisAmountPaid: nonEmptyString,
          whenWasThisAmountPaid: nonEmptyString,
        }),
      };
    }
  }

  return {};
};

const defenceAdmittedPartRoute = (
  responseType: DefendantResponseSpecType,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (responseType === DefendantResponseSpecType.PART_ADMISSION) {
    if (defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_PAID)
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'specDefenceAdmittedRequired'
          : 'specDefenceAdmitted2Required']: z.literal('Yes'),
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToAdmittedClaim'
          : 'respondToAdmittedClaim2']: z.looseObject({
          howMuchWasPaid: nonEmptyString,
          whenWasThisAmountPaid: nonEmptyString,
          howWasThisAmountPaid: nonEmptyString,
        }),
      };

    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specDefenceAdmittedRequired'
        : 'specDefenceAdmitted2Required']: z.literal('No'),
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondToAdmittedClaimOwingAmount'
        : 'respondToAdmittedClaimOwingAmount2']: nonEmptyString,
    };
  }

  return {};
};

const whenWillClaimBePaid = (
  responseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType === DefendantResponseSpecType.FULL_ADMISSION ||
    (responseType === DefendantResponseSpecType.PART_ADMISSION &&
      defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID)
  )
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'defenceAdmitPartPaymentTimeRouteRequired'
        : 'defenceAdmitPartPaymentTimeRouteRequired2']: z.literal(paymentTypeSpec),
      ...(paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE
        ? {
            [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
              ? 'respondToClaimAdmitPartLRspec'
              : 'respondToClaimAdmitPartLRspec2']: z.looseObject({
              whenWillThisAmountBePaid: nonEmptyString,
            }),
          }
        : {}),
    };

  return {};
};

const defendant1FinancialDetails = (
  responseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (responseType === DefendantResponseSpecType.FULL_ADMISSION ||
      responseType === DefendantResponseSpecType.PART_ADMISSION) &&
    (paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE ||
      paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN) &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
  ) {
    return {
      respondent1BankAccountList: z.array(z.looseObject({})).min(1),
      disabilityPremiumPayments: yesNoSchema,
      severeDisabilityPremiumPayments: yesNoSchema,
      respondent1DQHomeDetails: z.looseObject({}),
      respondent1PartnerAndDependent: z.looseObject({}),
      defenceAdmitPartEmploymentTypeRequired: yesNoSchema,
      respondToClaimAdmitPartEmploymentTypeLRspec: z.array(nonEmptyString).min(1),
      responseClaimAdmitPartEmployer: z.looseObject({}),
      specDefendant1SelfEmploymentDetails: z.looseObject({}),
      respondent1CourtOrderPaymentOption: yesNoSchema,
      respondent1CourtOrderDetails: z.array(z.looseObject({})).min(1),
      respondent1LoanCreditOption: yesNoSchema,
      respondent1LoanCreditDetails: z.array(z.looseObject({})).min(1),
      respondent1DQCarerAllowanceCreditFullAdmission: yesNoSchema,
      respondent1DQRecurringIncomeFA: z.array(z.looseObject({})).min(1),
      respondent1DQRecurringExpensesFA: z.array(z.looseObject({})).min(1),
      responseToClaimAdmitPartWhyNotPayLRspec: nonEmptyString,
    };
  }

  return {};
};

const defendant2FinancialDetails = (
  responseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  claimType: ClaimType,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (responseType === DefendantResponseSpecType.FULL_ADMISSION ||
      responseType === DefendantResponseSpecType.PART_ADMISSION) &&
    (paymentTypeSpec === PaymentTypeSpec.BY_SET_DATE ||
      paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN) &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    (claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
  ) {
    return {
      respondent2BankAccountList: z.array(z.looseObject({})).min(1),
      respondent2DQHomeDetails: z.looseObject({}),
      respondent2PartnerAndDependent: z.looseObject({}),
      respondent2CourtOrderPaymentOption: yesNoSchema,
      respondent2LoanCreditOption: yesNoSchema,
      respondent2DQCarerAllowanceCredit: yesNoSchema,
      responseToClaimAdmitPartWhyNotPayLRspec2: nonEmptyString,
    };
  }

  return {};
};

const defendant1RepaymentPlan = (
  responseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (responseType === DefendantResponseSpecType.FULL_ADMISSION ||
      responseType === DefendantResponseSpecType.PART_ADMISSION) &&
    paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
  ) {
    return {
      respondent1RepaymentPlan: z.looseObject({
        firstRepaymentDate: nonEmptyString,
        paymentAmount: nonEmptyString,
        repaymentFrequency: nonEmptyString,
      }),
    };
  }

  return {};
};

const defendant2RepaymentPlan = (
  responseType: DefendantResponseSpecType,
  paymentTypeSpec: PaymentTypeSpec,
  claimType: ClaimType,
  defenceAdmittedPartRoute: DefenceAdmittedPartRouteSpec,
  defendantSolicitorParty: Party,
) => {
  if (
    (responseType === DefendantResponseSpecType.FULL_ADMISSION ||
      responseType === DefendantResponseSpecType.PART_ADMISSION) &&
    paymentTypeSpec === PaymentTypeSpec.REPAYMENT_PLAN &&
    defenceAdmittedPartRoute === DefenceAdmittedPartRouteSpec.HAS_NOT_PAID &&
    (claimType === ClaimType.ONE_VS_TWO_SAME_SOL ||
      defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_2)
  ) {
    return {
      respondent2RepaymentPlan: z.looseObject({
        firstRepaymentDate: nonEmptyString,
        paymentAmount: nonEmptyString,
        repaymentFrequency: nonEmptyString,
      }),
    };
  }

  return {};
};

const upload = (responseType: DefendantResponseSpecType, defendantSolicitorParty: Party) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  )
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'detailsOfWhyDoesYouDisputeTheClaim'
        : 'detailsOfWhyDoesYouDisputeTheClaim2']: nonEmptyString,
      // [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
      //   ? 'respondent1SpecDefenceResponseDocument'
      //   : 'respondent2SpecDefenceResponseDocument']: z.looseObject({}),
    };

  return {};
};

const timeline = (responseType: DefendantResponseSpecType, defendantSolicitorParty: Party) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  )
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specClaimResponseTimelineList'
        : 'specClaimResponseTimelineList2']: nonEmptyString,
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specResponseTimelineOfEvents'
        : 'specResponseTimelineOfEvents2']: z
        .array(
          z.looseObject({
            value: z.strictObject({
              timelineDate: nonEmptyString,
              timelineDescription: nonEmptyString,
            }),
          }),
        )
        .min(1),
    };

  return {};
};

const mediationContactInformation = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.SMALL_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'resp1MediationContactInfo'
        : 'resp2MediationContactInfo']: z.looseObject({}),
    };
  }

  return {};
};

const mediationAvailability = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.SMALL_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'resp1MediationAvailability'
        : 'resp2MediationAvailability']: z.looseObject({
        isMediationUnavailablityExists: yesNoSchema,
        unavailableDatesForMediation: z.array(z.looseObject({})).min(1),
      }),
    };
  }

  return {};
};

const deterWithoutHearing = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.SMALL_CLAIM
  ) {
    return {
      // [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
      //   ? 'deterWithoutHearingRespondent1'
      //   : 'deterWithoutHearingRespondent2']: z.looseObject({
      //   deterWithoutHearingYesNo: yesNoSchema,
      //   deterWithoutHearingWhyNot: nonEmptyString,
      // }),
    };
  }

  return {};
};

const fileDirectionsQuestionnaire = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQFileDirectionsQuestionnaire'
        : 'respondent2DQFileDirectionsQuestionnaire']: z.strictObject({
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
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.FAST_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQFixedRecoverableCosts'
        : 'respondent2DQFixedRecoverableCosts']: z.strictObject({
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
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.INTERMEDIATE_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQFixedRecoverableCostsIntermediate'
        : 'respondent2DQFixedRecoverableCostsIntermediate']: z.strictObject({
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

const disclosureOfElectronicDocumentsLRspec = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specRespondent1DQDisclosureOfElectronicDocuments'
        : 'specRespondent2DQDisclosureOfElectronicDocuments']: z.strictObject({
        reachedAgreement: yesNoSchema,
        agreementLikely: yesNoSchema,
        reasonForNoAgreement: nonEmptyString,
      }),
    };
  }

  return {};
};

const disclosureOfNonElectronicDocumentsLRspec = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'specRespondent1DQDisclosureOfNonElectronicDocuments'
        : 'specRespondent2DQDisclosureOfNonElectronicDocuments']: z.strictObject({
        bespokeDirections: nonEmptyString,
      }),
    };
  }

  return {};
};

const disclosureReport = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    (claimTrack === ClaimTrack.FAST_CLAIM ||
      claimTrack === ClaimTrack.INTERMEDIATE_CLAIM ||
      claimTrack === ClaimTrack.MULTI_CLAIM)
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQDisclosureReport'
        : 'respondent2DQDisclosureReport']: z.strictObject({
        disclosureFormFiledAndServed: yesNoSchema,
        disclosureProposalAgreed: yesNoSchema,
        draftOrderNumber: nonEmptyString,
      }),
    };
  }

  return {};
};

const experts = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondToClaimExperts'
          : 'respondToClaimExperts2']: z.looseObject({
          firstName: nonEmptyString,
          lastName: nonEmptyString,
          emailAddress: nonEmptyString,
          phoneNumber: nonEmptyString,
          whyRequired: nonEmptyString,
          estimatedCost: nonEmptyString,
        }),
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'responseClaimExpertSpecRequired'
          : 'responseClaimExpertSpecRequired2']: yesNoSchema,
      };

    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQExperts'
        : 'respondent2DQExperts']: z.strictObject({
        expertRequired: yesNoSchema,
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
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQWitnessesSmallClaim'
          : 'respondent2DQWitnessesSmallClaim']: z.strictObject({
          witnessesToAppear: yesNoSchema,
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
        }),
      };

    if (defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1) {
      return {
        respondent1DQWitnessesRequiredSpec: yesNoSchema,
        respondent1DQWitnessesDetailsSpec: z
          .array(
            z.strictObject({
              id: nonEmptyString,
              value: z.looseObject({
                partyID: nonEmptyString.optional(),
                firstName: nonEmptyString,
                lastName: nonEmptyString,
                phoneNumber: nonEmptyString,
                emailAddress: nonEmptyString,
                reasonForWitness: nonEmptyString,
                eventAdded: nonEmptyString.optional(),
                dateAdded: nonEmptyString.optional(),
              }),
            }),
          )
          .min(1),
      };
    }

    return {
      respondent2DQWitnesses: z.strictObject({
        witnessesToAppear: yesNoSchema,
        details: z
          .array(
            z.strictObject({
              id: nonEmptyString,
              value: z.strictObject({
                partyID: nonEmptyString.optional(),
                firstName: nonEmptyString,
                lastName: nonEmptyString,
                phoneNumber: nonEmptyString,
                emailAddress: nonEmptyString,
                reasonForWitness: nonEmptyString,
                eventAdded: nonEmptyString.optional(),
                dateAdded: nonEmptyString.optional(),
              }),
            }),
          )
          .min(1),
      }),
    };
  }

  return {};
};

const language = (responseType: DefendantResponseSpecType, defendantSolicitorParty: Party) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  )
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQLanguage'
        : 'respondent2DQLanguage']: z.strictObject({
        court: nonEmptyString,
        documents: nonEmptyString,
      }),
    };

  return {};
};

const hearing = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  ) {
    if (claimTrack === ClaimTrack.SMALL_CLAIM)
      return {
        [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
          ? 'respondent1DQHearingSmallClaim'
          : 'respondent2DQHearingSmallClaim']: z.looseObject({
          unavailableDatesRequired: yesNoSchema,
          smallClaimUnavailableDate: z.array(z.looseObject({})).min(1),
        }),
        // [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        //   ? 'SmallClaimHearingInterpreterRequired'
        //   : 'SmallClaimHearingInterpreter2Required']: yesNoSchema,
        // [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        //   ? 'SmallClaimHearingInterpreterDescription'
        //   : 'smallClaimHearingInterpreterDescription2']: nonEmptyString,
      };

    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQHearing'
        : 'respondent2DQHearing']: z.looseObject({
        unavailableDatesRequired: yesNoSchema,
      }),
    };
  }

  return {};
};

const requestedCourtLocation = (
  responseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondToCourtLocation'
        : 'respondToCourtLocation2']: z.looseObject({
        reasonForHearingAtSpecificCourt: nonEmptyString.optional(),
      }),
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQRemoteHearingLRspec'
        : 'respondent2DQRemoteHearingLRspec']: z.looseObject({
        remoteHearingRequested: yesNoSchema,
        reasonForRemoteHearing: nonEmptyString,
      }),
    };
  }

  return {};
};

const hearingSupport = (
  responseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQHearingSupport'
        : 'respondent2DQHearingSupport']: z.strictObject({
        supportRequirements: yesNoSchema,
        supportRequirementsAdditional: nonEmptyString,
      }),
    };
  }

  return {};
};

const vulnerabilityQuestions = (
  responseType: DefendantResponseSpecType,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM
  )
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQVulnerabilityQuestions'
        : 'respondent2DQVulnerabilityQuestions']: z.strictObject({
        vulnerabilityAdjustmentsRequired: yesNoSchema,
        vulnerabilityAdjustments: nonEmptyString,
      }),
    };

  return {};
};

const applications = (
  responseType: DefendantResponseSpecType,
  claimTrack: ClaimTrack,
  defendantSolicitorParty: Party,
) => {
  if (
    responseType !== DefendantResponseSpecType.FULL_ADMISSION &&
    responseType !== DefendantResponseSpecType.COUNTER_CLAIM &&
    claimTrack === ClaimTrack.FAST_CLAIM
  ) {
    return {
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'additionalInformationForJudge'
        : 'additionalInformationForJudge2']: nonEmptyString,
      [defendantSolicitorParty === partys.DEFENDANT_SOLICITOR_1
        ? 'respondent1DQFutureApplications'
        : 'respondent2DQFutureApplications']: z.strictObject({
        intentionToMakeFutureApplications: yesNoSchema,
        whatWillFutureApplicationsBeMadeFor: nonEmptyString,
      }),
    };
  }

  return {};
};

const defendantResponseSpecSchemaComponents = {
  responseClaimTrack,
  singleResponse,
  responseConfirmNameAddress,
  responseConfirmDetails,
  respondentResponseType,
  defenceRoute,
  defenceAdmittedPartRoute,
  upload,
  timeline,
  whenWillClaimBePaid,
  defendant1FinancialDetails,
  defendant2FinancialDetails,
  defendant1RepaymentPlan,
  defendant2RepaymentPlan,
  mediationContactInformation,
  mediationAvailability,
  deterWithoutHearing,
  fileDirectionsQuestionnaire,
  fixedRecoverableCosts,
  fixedRecoverableCostsIntermediate,
  disclosureOfElectronicDocumentsLRspec,
  disclosureOfNonElectronicDocumentsLRspec,
  disclosureReport,
  experts,
  witnesses,
  language,
  hearing,
  requestedCourtLocation,
  hearingSupport,
  vulnerabilityQuestions,
  applications,
};

export default defendantResponseSpecSchemaComponents;
