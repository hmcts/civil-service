import { z } from 'zod';
import BaseSchemaBuilder from '../../../../base/base-schema-builder';
import NotSuitableSdoOption from '../../../../constants/ccd-events/non-suitable-sdo/not-suitable-sdo-option';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ZodHelper from '../../../../helpers/zod-helper';
import CCDCaseData from '../../../../models/ccd-case-data';
import notSuitableSdoSchemaBuilderComponents from './not-suitable-sdo-schema-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class NotSuitableSdoSchemaBuilder extends BaseSchemaBuilder {
  async buildChangeLocation(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission);
  }

  async buildOtherReasons(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema(caseDataBeforeSubmission, {
      notSuitableSdoOption: NotSuitableSdoOption.OTHER_REASONS,
    });
  }

  protected async buildSchema(
    caseDataBeforeSubmission?: CCDCaseData,
    {
      notSuitableSdoOption = NotSuitableSdoOption.CHANGE_LOCATION,
    }: {
      notSuitableSdoOption?: NotSuitableSdoOption;
    } = {},
  ): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...notSuitableSdoSchemaBuilderComponents.notSuitableSdo(notSuitableSdoOption),
    });
  }
}
