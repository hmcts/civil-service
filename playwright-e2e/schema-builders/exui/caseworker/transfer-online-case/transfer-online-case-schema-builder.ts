import { z } from 'zod';
import BaseSchemaBuilder from '../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ZodHelper from '../../../../helpers/zod-helper';
import CCDCaseData from '../../../../models/ccd-case-data';
import transferOnlineCaseSchemaBuilderComponents from './transfer-online-case-schema-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class TransferOnlineCaseSchemaBuilder extends BaseSchemaBuilder {
  async buildData(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...transferOnlineCaseSchemaBuilderComponents.transferOnlineCase,
    });
  }
}
