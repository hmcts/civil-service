import { z } from 'zod';
import BaseSchemaBuilder from '../../../../base/base-schema-builder';
import ClaimTrack from '../../../../constants/cases/claim-track';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ZodHelper from '../../../../helpers/zod-helper';
import CCDCaseData from '../../../../models/ccd-case-data';
import generateDirectionsOrderSchemaBuilderComponents from './generate-directions-order-schema-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class GenerateDirectionsOrderSchemaBuilder extends BaseSchemaBuilder {
  async buildAssistedOrder(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildFreeFormOrder(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.FAST_CLAIM,
    });
  }

  async buildIntermediateOrder(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.INTERMEDIATE_CLAIM,
    });
  }

  async buildMultiOrder(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      claimTrack: ClaimTrack.MULTI_CLAIM,
    });
  }

  protected async buildSchema(
    caseDataBeforeSubmission?: CCDCaseData,
    {
      claimTrack = ClaimTrack.SMALL_CLAIM,
    }: {
      claimTrack?: ClaimTrack;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...generateDirectionsOrderSchemaBuilderComponents.finalOrderDocumentCollection(claimTrack),
    });
  }
}
