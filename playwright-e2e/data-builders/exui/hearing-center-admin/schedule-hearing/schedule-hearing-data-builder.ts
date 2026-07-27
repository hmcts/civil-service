import BaseDataBuilder from '../../../../base/base-data-builder';
import ClaimTrack from '../../../../constants/cases/claim-track';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import scheduleHearingDataBuilderComponents from './schedule-hearing-data-builder-components';

@AllMethodsStep()
export default class ScheduleHearingDataBuilder extends BaseDataBuilder {
  async buildFast() {
    return this.buildData({claimTrack: ClaimTrack.FAST_CLAIM});
  }

  async buildSmallClaim() {
    return this.buildData();
  }

  protected async buildData(
    {
      claimTrack = ClaimTrack.SMALL_CLAIM
    }: {
      claimTrack?: ClaimTrack
    } = {}) {
    return {
      ...scheduleHearingDataBuilderComponents.hearingNoticeSelect(claimTrack),
      ...scheduleHearingDataBuilderComponents.listingOrRelisting,
      ...scheduleHearingDataBuilderComponents.hearingDetails,
      ...scheduleHearingDataBuilderComponents.hearingInformation,
    };
  }
}
