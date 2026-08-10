import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import defaultJudgementSchemaComponents from './default-judgement-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class DefaultJudgementSchemaBuilder extends BaseSchemaBuilder {
  async build1v1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  async build1v2SS(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...defaultJudgementSchemaComponents.defendantDetails(),
      ...defaultJudgementSchemaComponents.hearingType(),
      ...defaultJudgementSchemaComponents.hearingSupportRequirementsFieldDJ(),
    });
  }
}
