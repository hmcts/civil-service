import { z } from 'zod';
import ClaimTypeUnspec from '../../../../../constants/ccd-events/create-claim/claim-type-unspec';
import PersonalInjuryType from '../../../../../constants/ccd-events/create-claim/personal-injury-type';
import ClaimTrack from '../../../../../constants/cases/claim-track';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import PersonalInjuryClaimTypeUnspecObjs from '../../../../../models/ccd-events/create-claim/claim-type-unspec-objs';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';

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

const claimValueSchema = z.strictObject({
  statementOfValueInPennies: nonEmptyString,
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

const litigationFriendSchema = z.strictObject({
  partyID: nonEmptyString,
  firstName: nonEmptyString,
  lastName: nonEmptyString,
  emailAddress: nonEmptyString,
  phoneNumber: nonEmptyString,
  hasSameAddressAsLitigant: yesNoSchema,
  primaryAddress: addressSchema,
  flags: flagsSchema,
  certificateOfSuitability: z.unknown(),
});

const organisationPolicySchema = z.strictObject({
  OrgPolicyCaseAssignedRole: nonEmptyString,
  OrgPolicyReference: z.string().optional(),
  Organisation: z
    .looseObject({
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

const courtLocationSchema = z.strictObject({
  applicantPreferredCourt: nonEmptyString,
  caseLocation: z.strictObject({
    region: nonEmptyString,
    baseLocation: nonEmptyString,
  }),
});

const claimIssuedPaymentDetailsSchema = z.strictObject({
  customerReference: nonEmptyString,
});

const claimIssuedPbaDetailsSchema = z.strictObject({
  fee: feeSchema,
  applicantsPbaAccounts: pbaAccountsSchema,
  serviceRequestReference: nonEmptyString,
});

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

const references = {
  unassignedCaseListDisplayOrganisationReferences: nonEmptyString,
  caseListDisplayDefendantSolicitorReferences: nonEmptyString,
};

const solicitorReferences = (claimType: ClaimType) => {
  if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      solicitorReferences: solicitorReferencesWithRespondent2Schema,
    };
  } else if (ClaimTypeHelper.isDefendant2RepresentedNotSame(claimType)) {
    return {
      solicitorReferences: solicitorReferencesSchema,
      respondentSolicitor2Reference: nonEmptyString,
    };
  } else {
    return {
      solicitorReferences: solicitorReferencesSchema,
    };
  }
};

const claimantCourt = {
  courtLocation: courtLocationSchema,
  caseManagementLocation: caseManagementLocationSchema,
  caseManagementCategory: caseManagementCategorySchema,
  locationName: nonEmptyString,
  applicant1DQRemoteHearing: z.strictObject({
    remoteHearingRequested: yesNoSchema,
    reasonForRemoteHearing: nonEmptyString,
  }),
};

const claimant1 = (claimant1PartyType: ClaimantDefendantPartyType) => ({
  applicant1: partySchema(claimant1PartyType),
  applicant1LitigationFriendRequired: yesNoSchema,
  applicant1LitigationFriend: litigationFriendSchema,
});

const claimantSolicitor1 = {
  applicantSolicitor1UserDetails: z.strictObject({
    email: nonEmptyString,
    id: nonEmptyString,
  }),
  applicant1OrganisationPolicy: organisationPolicySchema,
  applicantSolicitor1ServiceAddressRequired: yesNoSchema,
  applicantSolicitor1ServiceAddress: addressSchema,
};

const claimant2 = (claimType: ClaimType, claimant2PartyType: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isClaimant2(claimType)) {
    return {
      addApplicant2: z.literal('Yes'),
      applicant2: partySchema(claimant2PartyType),
      applicant2LitigationFriendRequired: yesNoSchema,
      applicant2LitigationFriend: litigationFriendSchema,
    };
  } else {
    return {
      addApplicant2: z.literal('No'),
      applicant2: z.undefined().optional(),
      applicant2LitigationFriendRequired: z.undefined().optional(),
      applicant2LitigationFriend: z.undefined().optional(),
    };
  }
};

const defendant1 = (defendant1PartyType: ClaimantDefendantPartyType) => ({
  respondent1: partySchema(defendant1PartyType),
  respondent1DetailsForClaimDetailsTab: detailsForClaimTabPartySchema(defendant1PartyType),
  defendant1LIPAtClaimIssued: yesNoSchema,
});

const defendantSolicitor1 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Represented(claimType)) {
    return {
      respondent1Represented: z.literal('Yes'),
      respondent1OrgRegistered: yesNoSchema,
      respondent1OrganisationPolicy: organisationPolicySchema,
      respondentSolicitor1EmailAddress: nonEmptyString,
      respondentSolicitor1ServiceAddressRequired: yesNoSchema,
      respondentSolicitor1ServiceAddress: addressSchema,
    };
  } else {
    return {
      respondent1Represented: z.literal('No'),
      respondent1OrganisationPolicy: organisationPolicySchema,
      respondentSolicitor1EmailAddress: z.undefined().optional(),
      respondentSolicitor1ServiceAddressRequired: z.undefined().optional(),
      respondentSolicitor1ServiceAddress: z.undefined().optional(),
    };
  }
};

const defendant2 = (claimType: ClaimType, defendant2PartyType: ClaimantDefendantPartyType) => {
  if (ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      addRespondent2: z.literal('Yes'),
      respondent2: partySchema(defendant2PartyType),
      respondent2DetailsForClaimDetailsTab: detailsForClaimTabPartySchema(defendant2PartyType),
      defendant2LIPAtClaimIssued: yesNoSchema,
    };
  } else {
    return {
      addRespondent2: z.literal('No'),
      respondent2: z.undefined().optional(),
      respondent2DetailsForClaimDetailsTab: z.undefined().optional(),
      defendant2LIPAtClaimIssued: z.undefined().optional(),
    };
  }
};

const respondent2SolicitorFields = {
  respondent2OrgRegistered: yesNoSchema,
  respondentSolicitor2EmailAddress: nonEmptyString,
  respondentSolicitor2ServiceAddressRequired: yesNoSchema,
  respondentSolicitor2ServiceAddress: addressSchema,
};

const respondent2SolicitorFieldsAbsent = Object.fromEntries(
  Object.keys(respondent2SolicitorFields).map((fieldName) => [
    fieldName,
    z.undefined().optional(),
  ]),
);

const defendant2Representation = (claimType: ClaimType) => {
  if (!ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      respondent2Represented: z.undefined().optional(),
      respondent2SameLegalRepresentative: z.undefined().optional(),
      ...respondent2SolicitorFieldsAbsent,
    };
  } else if (ClaimTypeHelper.isDefendant2RepresentedNotSame(claimType)) {
    return {
      respondent2Represented: z.literal('Yes'),
      respondent2SameLegalRepresentative: z.literal('No'),
      ...respondent2SolicitorFields,
    };
  } else if (claimType === ClaimType.ONE_VS_TWO_SAME_SOL) {
    return {
      respondent2Represented: z.literal('Yes'),
      respondent2SameLegalRepresentative: z.literal('Yes'),
    };
  } else {
    return {
      respondent2Represented: z.literal('No'),
      respondent2SameLegalRepresentative: z.literal('No').optional(),
      ...respondent2SolicitorFieldsAbsent,
    };
  }
};

const lipResponseArtifacts = (claimType: ClaimType) => {
  if (!ClaimTypeHelper.isDefendant2(claimType)) {
    return {
      defendant1LIPAtClaimIssued: ClaimTypeHelper.isDefendant1Unrepresented(claimType)
        ? z.literal('Yes')
        : z.literal('No'),
      defendant2LIPAtClaimIssued: z.undefined().optional(),
      claimIssuedPBADetails: claimIssuedPbaDetailsSchema,
    };
  } else if (ClaimTypeHelper.isDefendant2Unrepresented(claimType)) {
    return {
      defendant1LIPAtClaimIssued: ClaimTypeHelper.isDefendant1Unrepresented(claimType)
        ? z.literal('Yes')
        : z.literal('No'),
      defendant2LIPAtClaimIssued: z.literal('Yes'),
      claimIssuedPBADetails: claimIssuedPbaDetailsSchema,
    };
  } else {
    return {
      defendant1LIPAtClaimIssued: ClaimTypeHelper.isDefendant1Unrepresented(claimType)
        ? z.literal('Yes')
        : z.literal('No'),
      defendant2LIPAtClaimIssued: z.literal('No'),
      claimIssuedPBADetails: claimIssuedPbaDetailsSchema,
    };
  }
};

const claimTypeUnspec = (claimTypeUnSpec: ClaimTypeUnspec | PersonalInjuryClaimTypeUnspecObjs) => {
  const claimType =
    typeof claimTypeUnSpec === 'object' ? claimTypeUnSpec.claimTypeUnspec : claimTypeUnSpec;

  if (typeof claimTypeUnSpec === 'object' && claimType === ClaimTypeUnspec.PERSONAL_INJURY) {
    return {
      claimTypeUnSpec: z.literal(ClaimTypeUnspec.PERSONAL_INJURY),
      personalInjuryType: z.literal(claimTypeUnSpec.personalInjuryType),
      ...(claimTypeUnSpec.personalInjuryType === PersonalInjuryType.PERSONAL_INJURY_OTHER
        ? { personalInjuryTypeOther: nonEmptyString }
        : {}),
    };
  }

  if (claimType === ClaimTypeUnspec.OTHER) {
    return {
      claimTypeUnSpec: z.literal(ClaimTypeUnspec.OTHER),
      claimTypeOther: nonEmptyString,
    };
  }

  return {
    claimTypeUnSpec: z.literal(claimType),
  };
};

const claimDetails = (claimTrack: ClaimTrack) => ({
  allPartyNames: nonEmptyString,
  submittedDate: nonEmptyString,

  claimType: nonEmptyString,
  allocatedTrack: z.literal(claimTrack),

  anyRepresented: nonEmptyString,

  detailsOfClaim: nonEmptyString,
  claimFee: feeSchema,
  claimValue: claimValueSchema,
  paymentTypePBA: nonEmptyString,

  legacyCaseReference: nonEmptyString,
  caseNamePublic: nonEmptyString,
  caseNameHmctsInternal: nonEmptyString,

  applicantSolicitor1PbaAccounts: pbaAccountsSchema,
  applicantSolicitor1PbaAccountsIsEmpty: yesNoSchema,
  claimIssuedPaymentDetails: claimIssuedPaymentDetailsSchema,
});

const statementOfTruth = {
  applicantSolicitor1ClaimStatementOfTruth: statementOfTruthSchema,
};

const createClaimResponseSchema = {
  references,
  claimantCourt,
  claimant1,
  claimantSolicitor1,
  claimant2,
  solicitorReferences,
  defendant1,
  defendantSolicitor1,
  defendant2,
  defendant2Representation,
  claimTypeUnspec,
  claimDetails,
  statementOfTruth,
  lipResponseArtifacts,
};

export default createClaimResponseSchema;
