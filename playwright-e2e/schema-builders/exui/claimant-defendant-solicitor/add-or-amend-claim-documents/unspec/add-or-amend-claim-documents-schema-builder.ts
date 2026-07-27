import { z } from 'zod';
import BaseSchemaBuilder from '../../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import ZodHelper from '../../../../../helpers/zod-helper';
import CCDCaseData from '../../../../../models/ccd-case-data';
import addOrAmendClaimDocumentsSchemaBuilderComponents from './add-or-amend-claim-documents-schema-builder-components';

@AllMethodsStep()
export default class AddOrAmendClaimDocumentsSchemaBuilder extends BaseSchemaBuilder {
  async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const schemaCaseData = structuredClone(caseDataBeforeSubmission);

    const baseSchema = ZodHelper.createSchemaFromJson(schemaCaseData, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...addOrAmendClaimDocumentsSchemaBuilderComponents.servedDocumentFiles,
    });
  }
}
