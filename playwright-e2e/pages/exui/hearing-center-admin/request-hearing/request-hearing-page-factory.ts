import BasePageFactory from '../../../../base/base-page-factory';
import HearingRequirementsPage from './hearing-requirements/hearing-requirements-page';
import HearingFacilitiesPage from './hearing-facilities/hearing-facilities-page';
import HearingStagePage from './hearing-stage/hearing-stage-page';
import HearingAttendancePage from './hearing-attendance/hearing-attendance-page';
import HearingVenuePage from './hearing-venue/hearing-venue-page';
import HearingJudgePage from './hearing-judge/hearing-judge-page';
import HearingTimingPage from './hearing-timing/hearing-timing-page';
import HearingAdditionalInstructionsPage from './hearing-additional-instructions/hearing-additional-instructions-page';
import HearingSubmitPage from './hearing-submit/hearing-submit-page';
import HearingConfirmPage from './hearing-confirm/hearing-confirm-page';
import HearingViewSummaryPage from './hearing-view-summary/hearing-view-summary-page';
import HearingEditSummaryPage from './hearing-edit-summary/hearing-edit-summary-page';
import HearingUpdateReasonPage from './hearing-change-reason/hearing-change-reason-page';
import HearingCancelPage from './hearing-cancel/hearing-cancel-page';
import CaseDetailsPage from '../../exui-dashboard/case-details/case-details-page';

export default class RequestHearingPageFactory extends BasePageFactory {
  get caseDetailsPage() {
    return new CaseDetailsPage(this.page);
  }

  get hearingRequirementsPage() {
    return new HearingRequirementsPage(this.page);
  }

  get hearingFacilitiesPage() {
    return new HearingFacilitiesPage(this.page);
  }

  get hearingStagePage() {
    return new HearingStagePage(this.page);
  }

  get hearingAttendancePage() {
    return new HearingAttendancePage(this.page);
  }

  get hearingVenuePage() {
    return new HearingVenuePage(this.page);
  }

  get hearingJudgePage() {
    return new HearingJudgePage(this.page);
  }

  get hearingTimingPage() {
    return new HearingTimingPage(this.page);
  }

  get hearingAdditionalInstructionsPage() {
    return new HearingAdditionalInstructionsPage(this.page);
  }

  get hearingSubmitPage() {
    return new HearingSubmitPage(this.page);
  }

  get hearingConfirmPage() {
    return new HearingConfirmPage(this.page);
  }

  get hearingViewSummaryPage() {
    return new HearingViewSummaryPage(this.page);
  }

  get hearingEditSummaryPage() {
    return new HearingEditSummaryPage(this.page);
  }

  get hearingChangeReasonPage() {
    return new HearingUpdateReasonPage(this.page);
  }

  get hearingCancelPage() {
    return new HearingCancelPage(this.page);
  }
}
