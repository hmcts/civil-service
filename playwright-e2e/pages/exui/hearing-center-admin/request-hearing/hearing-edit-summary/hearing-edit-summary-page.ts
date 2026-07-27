import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, button } from './hearing-edit-summary-content';
import { heading as facilitiesHeading } from '../hearing-facilities/hearing-facilities-content';
import { heading as judgeHeading } from '../hearing-judge/hearing-judge-content';
import { heading as attendanceHeading } from '../hearing-attendance/hearing-attendance-content';
import { heading as timingHeading } from '../hearing-timing/hearing-timing-content';
import { heading as additionalInstructionsHeading } from '../hearing-additional-instructions/hearing-additional-instructions-content';

@AllMethodsStep()
export default class HearingEditSummaryPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([super.expectHeading(heading)]);
  }

  async changeAdditionalFacilities() {
    await super.clickBySelector(button.change.additionalFacilities.selector);
    await super.expectHeading(facilitiesHeading);
  }

  async changeJudgeTypes() {
    await super.clickBySelector(button.change.judgeTypes.selector);
    await super.expectHeading(judgeHeading);
  }

  async changeAttendance() {
    await super.clickBySelector(button.change.participantAttendance.selector);
    await super.expectHeading(attendanceHeading);
  }

  async changeTiming() {
    await super.clickBySelector(button.change.timings.selector);
    await super.expectHeading(timingHeading);
  }

  async changeAdditionalInstructions() {
    await super.clickBySelector(button.change.additionalInstructions.selector);
    await super.expectHeading(additionalInstructionsHeading);
  }

  async continue() {
    await super.submitUpdatedRequest();
  }
}
