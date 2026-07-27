import { z } from 'zod';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import { Document } from '../../../../../models/ccd-case-data';

const nonEmptyString = z.string().min(1);

const deadlines = {
  claimDismissedDeadline: nonEmptyString,
  respondent1ResponseDeadline: nonEmptyString,
  respondent2ResponseDeadline: nonEmptyString.optional(),
}

const defendantSolicitorNotifyClaimDetailsOptionsSchema = z.strictObject({
  value: z.strictObject({
    code: nonEmptyString,
    label: nonEmptyString,
  }),
  list_items: z.array(
    z.strictObject({
      code: nonEmptyString,
      label: nonEmptyString,
    })
  ).min(1)
});

const defendantSolicitorNotifyClaimDetailsOptions = (claimType: ClaimType) => {
  if (
    ClaimTypeHelper.isDefendant1Unrepresented(claimType) ||
    ClaimTypeHelper.isDefendant2Unrepresented(claimType)
  ) {
    return {
      defendantSolicitorNotifyClaimDetailsOptions: z.union([
        defendantSolicitorNotifyClaimDetailsOptionsSchema,
        z.null(),
      ]),
    };
  }

  return {
    defendantSolicitorNotifyClaimDetailsOptions: defendantSolicitorNotifyClaimDetailsOptionsSchema
  };
};

const notification = {
  claimDetailsNotificationDate: nonEmptyString,
};

const undefine = {
  // addLegalRepDeadlineRes1: z.undefined().optional(),
  // addLegalRepDeadlineRes2: z.undefined().optional(),
};

const servedDocumentFiles = (particularOfClaimDocumentBeforeSubmission: Document[]) => {
  if (!particularOfClaimDocumentBeforeSubmission) {
    return {
      servedDocumentFiles: z.looseObject({
        particularsOfClaimDocument: z.array(
          z.looseObject({
            value: z.looseObject({
              document_url: nonEmptyString,
              document_binary_url: nonEmptyString,
              document_filename: nonEmptyString,
            }),
          }),
        ).min(1),
      }),
    };
  }
  
  return {};
};

const certificateOfService1 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Unrepresented(claimType)) {
    return {
      cosNotifyClaimDetails1: z.object({
        cosDateOfServiceForDefendant: nonEmptyString,
        cosDateDeemedServedForDefendant: nonEmptyString,
        cosServedDocumentFiles: nonEmptyString,
        cosRecipient: nonEmptyString,
        cosRecipientServeType: z.literal('HANDED'),
        cosRecipientServeLocation: nonEmptyString,
        cosRecipientServeLocationOwnerType: z.literal('FRIEND'),
        cosRecipientServeLocationType: z.literal('LAST_KNOWN_RESIDENCE'),
        cosSender: nonEmptyString,
        cosSenderFirm: nonEmptyString,
        cosEvidenceDocument: z.array(
          z.object({
            id: nonEmptyString,
            value: z.object({
              document_url: nonEmptyString,
              document_binary_url: nonEmptyString,
              document_filename: nonEmptyString,
            }).passthrough(),
          })
        ).min(1),
        cosUISenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
        cosSenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
      })
    };
  }

  return {};
};

const certificateOfService2 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2Unrepresented(claimType)) {
    return {
      cosNotifyClaimDetails2: z.object({
        cosDateOfServiceForDefendant: nonEmptyString,
        cosDateDeemedServedForDefendant: nonEmptyString,
        cosServedDocumentFiles: nonEmptyString,
        cosRecipient: nonEmptyString,
        cosRecipientServeType: z.literal('OTHER'),
        cosRecipientServeLocation: nonEmptyString,
        cosRecipientServeLocationOwnerType: z.literal('FRIEND'),
        cosRecipientServeLocationType: z.literal('PRINCIPAL_OFFICE_CORPORATION'),
        cosSender: nonEmptyString,
        cosSenderFirm: nonEmptyString,
        cosEvidenceDocument: z.array(
          z.object({
            id: nonEmptyString,
            value: z.object({
              document_url: nonEmptyString,
              document_binary_url: nonEmptyString,
              document_filename: nonEmptyString,
            }).passthrough(),
          })
        ).min(1),
        cosUISenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
        cosSenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
      })
    };
  }

  return {};
};

const notifyClaimDetailsSchemaBuilderComponents = {
  deadlines,
  defendantSolicitorNotifyClaimDetailsOptions,
  notification,
  servedDocumentFiles,
  certificateOfService1,
  certificateOfService2,
  undefine,
};

export default notifyClaimDetailsSchemaBuilderComponents;
