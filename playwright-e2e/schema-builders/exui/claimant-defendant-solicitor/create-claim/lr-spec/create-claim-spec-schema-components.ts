import { z } from 'zod';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import ClaimType from '../../../../../constants/cases/claim-type';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';

type SchemaShape = Record<string, z.ZodType>;

const yesNoSchema = z.enum(['Yes', 'No']);
const nonEmptyString = z.string().min(1);

const addressSchema = z.strictObject({
  AddressLine1: nonEmptyString,
  AddressLine2: z.string().optional(),
  AddressLine3: z.string().optional(),
  PostTown: nonEmptyString,
  County: z.string().optional(),
  Country: nonEmptyString,
  PostCode: nonEmptyString,
});

const feeSchema = z.strictObject({
  code: nonEmptyString,
  version: nonEmptyString,
  calculatedAmountInPence: nonEmptyString,
});

const flagsSchema = z.strictObject({
  partyName: nonEmptyString,
  roleOnCase: nonEmptyString,
});

const partyBaseSchema = {
  partyID: nonEmptyString,
  primaryAddress: addressSchema,
  partyName: nonEmptyString,
  partyTypeDisplayValue: nonEmptyString,
  partyEmail: z.string().optional(),
  partyPhone: z.string().optional(),
  flags: flagsSchema,
};

const individualPartySchema = z.strictObject({
  ...partyBaseSchema,
  type: z.literal('INDIVIDUAL'),
  individualTitle: nonEmptyString,
  individualFirstName: nonEmptyString,
  individualLastName: nonEmptyString,
  individualDateOfBirth: nonEmptyString,
});

const soleTraderPartySchema = z.strictObject({
  ...partyBaseSchema,
  type: z.literal('SOLE_TRADER'),
  soleTraderTitle: nonEmptyString,
  soleTraderFirstName: nonEmptyString,
  soleTraderLastName: nonEmptyString,
  soleTraderTradingAs: nonEmptyString,
  soleTraderDateOfBirth: nonEmptyString,
});

const companyPartySchema = z.strictObject({
  ...partyBaseSchema,
  type: z.literal('COMPANY'),
  companyName: nonEmptyString,
  partyName: nonEmptyString,
});

const organisationPartySchema = z.strictObject({
  ...partyBaseSchema,
  type: z.literal('ORGANISATION'),
  organisationName: nonEmptyString,
  partyName: nonEmptyString,
});

export const partySchema = (partyType: ClaimantDefendantPartyType) => {
  switch (partyType.type) {
    case 'INDIVIDUAL':
      return individualPartySchema;
    case 'SOLE_TRADER':
      return soleTraderPartySchema;
    case 'COMPANY':
      return companyPartySchema;
    case 'ORGANISATION':
      return organisationPartySchema;
    default:
      throw new Error(`Unsupported party type: ${partyType.type}`);
  }
};

const detailsForClaimTabPartyBaseFields = {
  primaryAddress: addressSchema,
  partyName: nonEmptyString,
  partyTypeDisplayValue: nonEmptyString,
  partyEmail: z.string().optional(),
  partyPhone: z.string().optional(),
};

const individualDetailsForClaimTabPartySchema = z.strictObject({
  ...detailsForClaimTabPartyBaseFields,
  type: z.literal('INDIVIDUAL'),
  individualTitle: nonEmptyString,
  individualFirstName: nonEmptyString,
  individualLastName: nonEmptyString,
  individualDateOfBirth: nonEmptyString,
});

const soleTraderDetailsForClaimTabPartySchema = z.strictObject({
  ...detailsForClaimTabPartyBaseFields,
  type: z.literal('SOLE_TRADER'),
  soleTraderTitle: nonEmptyString,
  soleTraderFirstName: nonEmptyString,
  soleTraderLastName: nonEmptyString,
  soleTraderTradingAs: nonEmptyString,
});

const companyDetailsForClaimTabPartySchema = z.strictObject({
  ...detailsForClaimTabPartyBaseFields,
  type: z.literal('COMPANY'),
  companyName: nonEmptyString,
});

const organisationDetailsForClaimTabPartySchema = z.strictObject({
  ...detailsForClaimTabPartyBaseFields,
  type: z.literal('ORGANISATION'),
  organisationName: nonEmptyString,
});

export const detailsForClaimTabPartySchema = (partyType: ClaimantDefendantPartyType) => {
  switch (partyType.type) {
    case 'INDIVIDUAL':
      return individualDetailsForClaimTabPartySchema;
    case 'SOLE_TRADER':
      return soleTraderDetailsForClaimTabPartySchema;
    case 'COMPANY':
      return companyDetailsForClaimTabPartySchema;
    case 'ORGANISATION':
      return organisationDetailsForClaimTabPartySchema;
    default:
      throw new Error(`Unsupported party type: ${partyType.type}`);
  }
};

const organisationPolicySchema = z.strictObject({
  OrgPolicyCaseAssignedRole: nonEmptyString,
  OrgPolicyReference: z.string().optional(),
  Organisation: z
    .strictObject({
      OrganisationID: nonEmptyString,
    })
    .optional(),
});

const solicitorReferencesSchema = z.strictObject({
  applicantSolicitor1Reference: nonEmptyString,
  respondentSolicitor1Reference: nonEmptyString,
});

const solicitorReferencesWithRespondent2Schema = z.strictObject({
  applicantSolicitor1Reference: nonEmptyString,
  respondentSolicitor1Reference: nonEmptyString,
  respondentSolicitor2Reference: nonEmptyString,
});

const pbaAccountsSchema = z.strictObject({
  list_items: z
    .array(
      z.strictObject({
        code: nonEmptyString,
        label: nonEmptyString,
      }),
    )
    .min(1),
});

const statementOfTruthSchema = z.strictObject({
  name: nonEmptyString,
  role: nonEmptyString,
});

const caseManagementCategorySchema = z.strictObject({
  value: z.strictObject({
    code: nonEmptyString,
    label: nonEmptyString,
  }),
  list_items: z
    .array(
      z.strictObject({
        id: nonEmptyString,
        value: z.strictObject({
          code: nonEmptyString,
          label: nonEmptyString,
        }),
      }),
    )
    .min(1),
});

const caseManagementLocationSchema = z.strictObject({
  region: nonEmptyString,
  baseLocation: nonEmptyString,
});

const claimIssuedPaymentDetailsSchema = z.strictObject({
  customerReference: nonEmptyString,
});

const claimIssuedPbaDetailsSchema = z.strictObject({
  fee: feeSchema,
  applicantsPbaAccounts: pbaAccountsSchema,
  serviceRequestReference: nonEmptyString,
});

const timelineOfEventsSchema = z
  .array(
    z.strictObject({
      id: nonEmptyString,
      value: z.strictObject({
        timelineDate: nonEmptyString,
        timelineDescription: nonEmptyString,
      }),
    }),
  )
  .min(1);

const evidenceListSchema = z
  .array(
    z.strictObject({
      id: nonEmptyString,
      value: z.strictObject({
        evidenceType: nonEmptyString,
        contractAndAgreementsEvidence: nonEmptyString,
      }),
    }),
  )
  .min(1);

const claimAmountBreakupSchema = z
    .array(
      z.strictObject({
        id: nonEmptyString,
        value: z.strictObject({
          claimReason: nonEmptyString,
          claimAmount: nonEmptyString,
        }),
      }),
    )
    .min(1);

const fixedCostsSchema = z.strictObject({
  claimFixedCosts: z.literal('Yes'),
  fixedCostAmount: nonEmptyString,
});

const respondentPinToPostSchema = z.strictObject({
  accessCode: nonEmptyString,
  expiryDate: nonEmptyString,
  respondentCaseRole: nonEmptyString,
});

const references: SchemaShape = {
  caseListDisplayDefendantSolicitorReferences: nonEmptyString,
  solicitorReferences: solicitorReferencesSchema,
};

const solicitorReferences = (claimType: ClaimType): SchemaShape => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      solicitorReferences: solicitorReferencesWithRespondent2Schema,
    };
  }

  return {
    solicitorReferences: solicitorReferencesSchema,
  };
};

const claimantCourt: SchemaShape = {
  caseManagementLocation: caseManagementLocationSchema,
  caseManagementCategory: caseManagementCategorySchema,
  locationName: nonEmptyString,
};

const claimant1 = (claimant1PartyType: ClaimantDefendantPartyType): SchemaShape => ({
  applicant1: partySchema(claimant1PartyType),
});

const claimantSolicitor1: SchemaShape = {
  applicantSolicitor1UserDetails: z.strictObject({
    email: nonEmptyString,
    id: nonEmptyString,
  }),
  applicant1OrganisationPolicy: organisationPolicySchema,
  specApplicantCorrespondenceAddressRequired: yesNoSchema,
  specApplicantCorrespondenceAddressdetails: addressSchema,
};

const claimant2 = (
  claimType: ClaimType,
  claimant2PartyType: ClaimantDefendantPartyType,
): SchemaShape => {
  if (ClaimTypeHelper.isClaimant2(claimType)) {
    return {
      addApplicant2: z.literal('Yes'),
      applicant2: partySchema(claimant2PartyType),
    };
  }

  return {
    addApplicant2: z.literal('No'),
    applicant2: z.undefined().optional(),
  };
};

const defendant1 = (defendant1PartyType: ClaimantDefendantPartyType): SchemaShape => ({
  respondent1: partySchema(defendant1PartyType),
  respondent1DetailsForClaimDetailsTab: detailsForClaimTabPartySchema(defendant1PartyType),
});

const defendantSolicitor1 = (claimType: ClaimType): SchemaShape => {
  if (ClaimTypeHelper.isDefendant1Represented(claimType)) {
    return {
      respondent1Represented: z.literal('Yes'),
      specRespondent1Represented: z.literal('Yes'),
      respondent1OrgRegistered: yesNoSchema,
      respondent1OrganisationPolicy: organisationPolicySchema,
      respondentSolicitor1EmailAddress: nonEmptyString,
      specRespondentCorrespondenceAddressRequired: yesNoSchema,
      specRespondentCorrespondenceAddressdetails: addressSchema,
      respondent1PinToPostLRspec: z.undefined().optional(),
    };
  }

  return {
    respondent1Represented: z.literal('No'),
    specRespondent1Represented: z.literal('No'),
    respondent1OrganisationPolicy: organisationPolicySchema,
    respondentSolicitor1EmailAddress: z.undefined().optional(),
    specRespondentCorrespondenceAddressRequired: z.undefined().optional(),
    specRespondentCorrespondenceAddressdetails: z.undefined().optional(),
    respondent1PinToPostLRspec:
      claimType === ClaimType.ONE_VS_ONE_LIP
        ? respondentPinToPostSchema
        : z.undefined().optional(),
  };
};

const defendant2 = (
  claimType: ClaimType,
  defendant2PartyType: ClaimantDefendantPartyType,
): SchemaShape => {
  if (ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      addRespondent2: z.literal('Yes'),
      respondent2: partySchema(defendant2PartyType),
      respondent2DetailsForClaimDetailsTab: detailsForClaimTabPartySchema(defendant2PartyType),
    };
  }

  return {
    addRespondent2: z.literal('No'),
    respondent2: z.undefined().optional(),
    respondent2DetailsForClaimDetailsTab: z.undefined().optional(),
  };
};

const respondent2SolicitorFields: SchemaShape = {
  respondent2OrgRegistered: yesNoSchema,
  respondentSolicitor2EmailAddress: nonEmptyString,
  specRespondent2CorrespondenceAddressRequired: yesNoSchema,
  specRespondent2CorrespondenceAddressdetails: addressSchema,
};

const respondent2SolicitorFieldsAbsent = Object.fromEntries(
  Object.keys(respondent2SolicitorFields).map((fieldName) => [
    fieldName,
    z.undefined().optional(),
  ]),
) as SchemaShape;

const defendant2Representation = (claimType: ClaimType): SchemaShape => {
  if (!ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      respondent2Represented: z.undefined().optional(),
      specRespondent2Represented: z.undefined().optional(),
      respondent2SameLegalRepresentative: z.undefined().optional(),
      respondent2OrganisationPolicy: organisationPolicySchema,
      ...respondent2SolicitorFieldsAbsent,
    };
  }

  if (ClaimTypeHelper.isDefendant2Represented(claimType)) {
    return {
      respondent2Represented: z.literal('Yes'),
      specRespondent2Represented: z.literal('Yes'),
      respondent2SameLegalRepresentative: z.literal(
        claimType === ClaimType.ONE_VS_TWO_SAME_SOL ? 'Yes' : 'No',
      ),
      respondent2OrganisationPolicy: organisationPolicySchema,
      ...respondent2SolicitorFields,
    };
  }

  return {
    respondent2Represented: z.literal('No'),
    specRespondent2Represented: z.literal('No'),
    respondent2SameLegalRepresentative:
      claimType === ClaimType.ONE_VS_TWO_LIPS ? z.literal('No') : z.undefined().optional(),
    respondent2OrganisationPolicy: organisationPolicySchema,
    ...respondent2SolicitorFieldsAbsent,
  };
};

const claimDetails = (): SchemaShape => ({
  allPartyNames: nonEmptyString,
  submittedDate: nonEmptyString,
  anyRepresented: nonEmptyString,
  detailsOfClaim: nonEmptyString,
  claimFee: feeSchema,
  paymentTypePBASpec: nonEmptyString,
  legacyCaseReference: nonEmptyString,
  caseNamePublic: nonEmptyString,
  caseNameHmctsInternal: nonEmptyString,
  applicantSolicitor1PbaAccounts: pbaAccountsSchema,
  applicantSolicitor1PbaAccountsIsEmpty: yesNoSchema,
  claimIssuedPaymentDetails: claimIssuedPaymentDetailsSchema,
  claimIssuedPBADetails: claimIssuedPbaDetailsSchema,
  isFlightDelayClaim: z.literal('No'),
  flightDelayDetails: z.unknown(),
  timelineOfEvents: timelineOfEventsSchema,
  speclistYourEvidenceList: evidenceListSchema,
  claimAmountBreakup: claimAmountBreakupSchema,
  claimAmountBreakupSummaryObject: nonEmptyString,
  totalClaimAmount: z.number(),
  claimInterest: z.literal('No'),
  calculatedInterest: nonEmptyString,
  totalInterest: z.number(),
  fixedCosts: fixedCostsSchema,
});

const statementOfTruth: SchemaShape = {
  applicantSolicitor1ClaimStatementOfTruth: statementOfTruthSchema,
};

const createClaimResponseSchema = {
  references,
  solicitorReferences,
  claimantCourt,
  claimant1,
  claimantSolicitor1,
  claimant2,
  defendant1,
  defendantSolicitor1,
  defendant2,
  defendant2Representation,
  claimDetails,
  statementOfTruth,
};

export default createClaimResponseSchema;
