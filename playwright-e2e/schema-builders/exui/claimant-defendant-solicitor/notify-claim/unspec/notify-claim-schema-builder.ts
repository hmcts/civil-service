import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import ClaimType from '../../../../../constants/cases/claim-type';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import notifyClaimSchemaBuilderComponents from './notify-claim-schema-builder-components';

@AllMethodsStep()
export default class NotifyClaimSchemaBuilder extends BaseSchemaBuilder {
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
      ...notifyClaimSchemaBuilderComponents.notification,
      ...notifyClaimSchemaBuilderComponents.defendantSolicitorNotifyClaimOptions(claimType),
      ...notifyClaimSchemaBuilderComponents.certificateOfService1(claimType),
      ...notifyClaimSchemaBuilderComponents.certificateOfService2(claimType),
    });
  }
}
