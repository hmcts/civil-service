import { z } from 'zod';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';

const nonEmptyString = z.string().min(1);

const defendantSolicitorNotifyClaimOptionsSchema = z.strictObject({
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

const notification = {
  claimNotificationDate: nonEmptyString,
  claimDetailsNotificationDeadline: nonEmptyString,
};

const defendantSolicitorNotifyClaimOptions = (claimType: ClaimType) => {
  if (
    ClaimTypeHelper.isDefendant1Unrepresented(claimType) ||
    ClaimTypeHelper.isDefendant2Unrepresented(claimType)
  ) {
    return {
      defendantSolicitorNotifyClaimOptions: z.union([
        defendantSolicitorNotifyClaimOptionsSchema,
        z.null(),
      ]),
    };
  }

  return {
    defendantSolicitorNotifyClaimOptions: defendantSolicitorNotifyClaimOptionsSchema
  };
};

const certificateOfService1 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Unrepresented(claimType)) {
    return {
      cosNotifyClaimDefendant1: z.object({
        cosDateOfServiceForDefendant: nonEmptyString,
        cosDateDeemedServedForDefendant: nonEmptyString,
        cosServedDocumentFiles: nonEmptyString,
        cosRecipient: nonEmptyString,
        cosRecipientServeType: z.literal('HANDED'),
        cosRecipientServeLocation: nonEmptyString,
        cosRecipientServeLocationOwnerType: z.literal('SOLICITOR'),
        cosRecipientServeLocationType: z.literal('USUAL_RESIDENCE'),
        cosSender: nonEmptyString,
        cosSenderFirm: nonEmptyString,
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
      cosNotifyClaimDefendant2: z.object({
        cosDateOfServiceForDefendant: nonEmptyString,
        cosDateDeemedServedForDefendant: nonEmptyString,
        cosServedDocumentFiles: nonEmptyString,
        cosRecipient: nonEmptyString,
        cosRecipientServeType: z.literal('POSTED'),
        cosRecipientServeLocation: nonEmptyString,
        cosRecipientServeLocationOwnerType: z.literal('DEFENDANT'),
        cosRecipientServeLocationType: z.literal('LAST_KNOWN_PRINCIPAL_PLACE_BUSINESS'),
        cosSender: nonEmptyString,
        cosSenderFirm: nonEmptyString,
        cosUISenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
        cosSenderStatementOfTruthLabel: z.array(nonEmptyString).optional(),
      })
    };
  }

  return {};
};

const notifyClaimSchemaBuilderComponents = {
  notification,
  defendantSolicitorNotifyClaimOptions,
  certificateOfService1,
  certificateOfService2,
};

export default notifyClaimSchemaBuilderComponents;
