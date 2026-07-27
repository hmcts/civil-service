import BasePageFactory from '../../../../base/base-page-factory';
import SubmitCreateClaimPage from '../../claimant-defendant-solicitor/create-claim/common/submit-create-claim/submit-create-claim-page';
import CreateCaseFlagsCommentsPage from './common/create-case-flags-comments/create-case-flags-comments-page';
import CreateCaseFlagsFlagTypeCaseLevelPage from './common/create-case-flags-flag-type-case-level/create-case-flags-flag-type-case-level-page';
import CreateCaseFlagsFlagTypePartyPage from './common/create-case-flags-flag-type-party/create-case-flags-flag-type-party-page';
import CreateCaseFlagsReasonableAdjustmentPage from './common/create-case-flags-reasonable-adjustment/create-case-flags-reasonable-adjustment-page';
import CreateCaseFlagsSpecialMeasurePage from './common/create-case-flags-special-measure/create-case-flags-special-measure-page';
import CreateCaseFlagsLocationPage from './unspec/create-case-flags-location/create-case-flags-location-page';
import CreateCaseFlagsLocation1v2DSPage from './unspec/create-case-flags-location/create-case-flags-location-1v2DS-page';
import CreateCaseFlagsLocationSpecPage from './lr-spec/create-case-flags-location-spec/create-case-flags-location-spec-page';
import CreateCaseFlagsLocation1v2DSSpecPage from './lr-spec/create-case-flags-location-spec/create-case-flags-location-1v2DS-spec-page';
import CreateCaseFlagsLocation2v1Page from './unspec/create-case-flags-location/create-case-flags-location-2v1-page';
import CreateCaseFlagsLocation2v1SpecPage from './lr-spec/create-case-flags-location-spec/create-case-flags-location-2v1-spec-page';

export default class CreateCaseFlagsPageFactory extends BasePageFactory {
  get createCaseFlagsLocationPage() {
    return new CreateCaseFlagsLocationPage(this.page);
  }

  get createCaseFlagsLocation2v1Page() {
    return new CreateCaseFlagsLocation2v1Page(this.page);
  }

  get createCaseFlagsLocation1v2DSPage() {
    return new CreateCaseFlagsLocation1v2DSPage(this.page);
  }

  get createCaseFlagsLocationSpecPage() {
    return new CreateCaseFlagsLocationSpecPage(this.page);
  }

  get createCaseFlagsLocation2v1SpecPage() {
    return new CreateCaseFlagsLocation2v1SpecPage(this.page);
  }

  get createCaseFlagsLocation1v2DSSpecPage() {
    return new CreateCaseFlagsLocation1v2DSSpecPage(this.page);
  }

  get createCaseFlagsFlagTypeCaseLevelPage() {
    return new CreateCaseFlagsFlagTypeCaseLevelPage(this.page);
  }

  get createCaseFlagsFlagTypePartyPage() {
    return new CreateCaseFlagsFlagTypePartyPage(this.page);
  }

  get createCaseFlagsSpecialMeasurePage() {
    return new CreateCaseFlagsSpecialMeasurePage(this.page);
  }

  get createCaseFlagsReasonableAdjustmentPage() {
    return new CreateCaseFlagsReasonableAdjustmentPage(this.page);
  }

  get createCaseFlagsCommentsPage() {
    return new CreateCaseFlagsCommentsPage(this.page);
  }

  get submitCreateCaseFlagsPage() {
    return new SubmitCreateClaimPage(this.page);
  }
}
