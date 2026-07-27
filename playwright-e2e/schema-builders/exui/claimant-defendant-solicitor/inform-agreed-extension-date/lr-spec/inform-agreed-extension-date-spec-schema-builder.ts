import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import informAgreedExtensionDateSpecSchemaComponents from './inform-agreed-extension-date-spec-schema-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class InformAgreedExtensionDateSpecSchemaBuilder extends BaseSchemaBuilder {
  async buildDataDS1(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  protected async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...informAgreedExtensionDateSpecSchemaComponents.extensionDate,
    });
  }
}
