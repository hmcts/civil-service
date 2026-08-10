import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import ClaimType from '../../../../../constants/cases/claim-type';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import notifyClaimDetailsSchemaBuilderComponents from './notify-claim-details-schema-builder-components';

@AllMethodsStep()
export default class NotifyClaimDetailsSchemaBuilder extends BaseSchemaBuilder {
  async build1vLIP(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    return this.buildSchema(caseDataBeforeSubmission, { claimType: ClaimType.ONE_VS_ONE_LIP });
  }

  async build1v2LRLIP(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    return this.buildSchema(caseDataBeforeSubmission, { claimType: ClaimType.ONE_VS_TWO_LR_LIP });
  }

  async build1v2LIPS(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    return this.buildSchema(caseDataBeforeSubmission, { claimType: ClaimType.ONE_VS_TWO_LIPS });
  }

  async buildSchema(
    caseDataBeforeSubmission?: CCDCaseData,
    {
      claimType = ClaimType.ONE_VS_ONE
    }: {
      claimType?: ClaimType
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...notifyClaimDetailsSchemaBuilderComponents.notification,
      ...notifyClaimDetailsSchemaBuilderComponents.defendantSolicitorNotifyClaimDetailsOptions(claimType),
      ...notifyClaimDetailsSchemaBuilderComponents.deadlines,
      ...(ClaimTypeHelper.isDefendant1Represented(claimType) || ClaimTypeHelper.isDefendant2Represented(claimType)
        ? notifyClaimDetailsSchemaBuilderComponents.servedDocumentFiles(caseDataBeforeSubmission?.servedDocumentFiles?.particularsOfClaimDocument!)
        : {}),
      ...notifyClaimDetailsSchemaBuilderComponents.certificateOfService1(claimType),
      ...notifyClaimDetailsSchemaBuilderComponents.certificateOfService2(claimType),
      ...notifyClaimDetailsSchemaBuilderComponents.undefine,
    });
  }
}
