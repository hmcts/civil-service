import { z } from 'zod';
import BaseSchemaBuilder from '../../../../base/base-schema-builder';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ZodHelper from '../../../../helpers/zod-helper';
import CCDCaseData from '../../../../models/ccd-case-data';
import scheduleHearingSchemaComponents from './schedule-hearing-schema-components';

@AllMethodsStep()
export default class ScheduleHearingSchemaBuilder extends BaseSchemaBuilder {
  async buildSchema(caseDataBeforeSubmission?: CCDCaseData): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...scheduleHearingSchemaComponents.hearingNoticeSelect,
      ...scheduleHearingSchemaComponents.listingOrRelisting,
      ...scheduleHearingSchemaComponents.hearingDetails,
      ...scheduleHearingSchemaComponents.hearingInformation,
      ...scheduleHearingSchemaComponents.hearingDocuments,
      ...scheduleHearingSchemaComponents.hearingFee,
      ...scheduleHearingSchemaComponents.hearingFeePBADetails,
      ...scheduleHearingSchemaComponents.hearingReferenceNumber,
    });
  }
}
