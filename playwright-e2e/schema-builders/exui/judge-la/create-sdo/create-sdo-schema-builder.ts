import { z } from 'zod';
import BaseSchemaBuilder from '../../../../base/base-schema-builder';
import SdoType from '../../../../constants/ccd-events/sdo/sdo-type';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ZodHelper from '../../../../helpers/zod-helper';
import CCDCaseData from '../../../../models/ccd-case-data';
import createSdoSchemaBuilderComponents from './create-sdo-schema-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildSchema'] })
export default class CreateSdoSchemaBuilder extends BaseSchemaBuilder {
  async buildSmallNoSumSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.SMALL_TRACK_NO_SUM });
  }

  async buildSmallSumSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.SMALL_TRACK_SUM });
  }

  async buildSmallSumDRHSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.SMALL_TRACK_SUM_DRH });
  }

  async buildSmallNoSumDRHSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({
      caseDataBeforeSubmission,
      sdoType: SdoType.SMALL_TRACK_NO_SUM_DRH,
    });
  }

  async buildFastSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.FAST_TRACK });
  }

  async buildFastNIHLSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.FAST_TRACK_NIHL });
  }

  async buildTrailSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.TRAIL });
  }

  async buildTrailNIHLSdo(caseDataBeforeSubmission?: CCDCaseData) {
    return this.buildSchema({ caseDataBeforeSubmission, sdoType: SdoType.TRAIL_NIHL });
  }

  protected async buildSchema({
    caseDataBeforeSubmission,
    sdoType = SdoType.SMALL_TRACK_NO_SUM,
  }: {
    caseDataBeforeSubmission?: CCDCaseData;
    sdoType?: SdoType;
  } = {}): Promise<z.ZodType> {
    const baseSchema = ZodHelper.createSchemaFromJson(caseDataBeforeSubmission, {
      strictObjects: false,
    }) as z.ZodObject<any>;

    return baseSchema.extend({
      ...createSdoSchemaBuilderComponents.sdo(sdoType),
      ...createSdoSchemaBuilderComponents.claimsTrack(sdoType),
      ...createSdoSchemaBuilderComponents.orderType(sdoType),
      ...createSdoSchemaBuilderComponents.fastTrack(sdoType),
      ...createSdoSchemaBuilderComponents.sdoR2FastTrack(sdoType),
      ...createSdoSchemaBuilderComponents.smallClaims(sdoType),
      ...createSdoSchemaBuilderComponents.sdoR2SmallClaims(sdoType),
      ...createSdoSchemaBuilderComponents.orderPreview,
    });
  }
}
