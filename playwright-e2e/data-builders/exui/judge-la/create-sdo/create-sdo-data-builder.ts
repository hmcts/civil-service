import BaseDataBuilder from '../../../../base/base-data-builder';
import SdoType from '../../../../constants/ccd-events/sdo/sdo-type';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import createSdoDataBuilderComponents from './create-sdo-data-builder-components';

@AllMethodsStep({ methodNamesToIgnore: ['buildData'] })
export default class CreateSdoDataBuilder extends BaseDataBuilder {
  async buildSmallNoSumSdo() {
    return this.buildData({ sdoType: SdoType.SMALL_TRACK_NO_SUM });
  }

  async buildSmallSumSdo() {
    return this.buildData({ sdoType: SdoType.SMALL_TRACK_SUM });
  }

  async buildSmallSumDRHSdo() {
    return this.buildData({ sdoType: SdoType.SMALL_TRACK_SUM_DRH });
  }

  async buildSmallNoSumDRHSdo() {
    return this.buildData({ sdoType: SdoType.SMALL_TRACK_NO_SUM_DRH });
  }

  async buildFastSdo() {
    return this.buildData({ sdoType: SdoType.FAST_TRACK });
  }

  async buildFastNIHLSdo() {
    return this.buildData({ sdoType: SdoType.FAST_TRACK_NIHL });
  }

  async buildTrailSdo() {
    return this.buildData({ sdoType: SdoType.TRAIL });
  }

  async buildTrailNIHLSdo() {
    return this.buildData({ sdoType: SdoType.TRAIL_NIHL });
  }

  protected async buildData({
    sdoType = SdoType.SMALL_TRACK_NO_SUM,
  }: {
    sdoType?: SdoType;
  } = {}) {
    return {
      ...createSdoDataBuilderComponents.sdo(sdoType),
      ...createSdoDataBuilderComponents.claimsTrack(sdoType),
      ...createSdoDataBuilderComponents.orderType(sdoType),
      ...createSdoDataBuilderComponents.fastTrack(sdoType),
      ...createSdoDataBuilderComponents.sdoR2FastTrack(sdoType),
      ...createSdoDataBuilderComponents.smallClaims(sdoType),
      ...createSdoDataBuilderComponents.sdoR2SmallClaims(sdoType),
      ...createSdoDataBuilderComponents.orderPreview,
    };
  }
}
