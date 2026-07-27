import { AllMethodsStep } from '../../../../decorators/test-steps';
import BaseTestData from '../../../../base/base-test-data';
import TestData from '../../../../models/test-utils/test-data';
import RequestHearingPageFactory from '../../../../pages/exui/hearing-center-admin/request-hearing/request-hearing-page-factory';

@AllMethodsStep()
export default class RequestHearingActions extends BaseTestData {
  private requestHearingPageFactory: RequestHearingPageFactory;

  constructor(requestHearingPageFactory: RequestHearingPageFactory, testData: TestData) {
    super(testData);
    this.requestHearingPageFactory = requestHearingPageFactory;
  }

  async requestNewHearing() {
    const { caseDetailsPage } = this.requestHearingPageFactory;
    await caseDetailsPage.requestHearing();
  }

  async checkRequirements() {
    const { hearingRequirementsPage } = this.requestHearingPageFactory;
    await hearingRequirementsPage.verifyContent(this.ccdCaseData);
    await hearingRequirementsPage.continue();
  }

  async addHearingFacilities() {
    const { hearingFacilitiesPage } = this.requestHearingPageFactory;
    await hearingFacilitiesPage.verifyContent(this.ccdCaseData);
    await hearingFacilitiesPage.selectAdditionalFacilities();
    await hearingFacilitiesPage.continue();
  }

  async addStage() {
    const { hearingStagePage } = this.requestHearingPageFactory;
    await hearingStagePage.verifyContent(this.ccdCaseData);
    await hearingStagePage.selectStage();
    await hearingStagePage.continue();
  }

  async addAttendance() {
    const { hearingAttendancePage } = this.requestHearingPageFactory;
    await hearingAttendancePage.verifyContent(this.ccdCaseData);
    await hearingAttendancePage.selectAttendance();
    await hearingAttendancePage.continue();
  }

  async addVenue() {
    const { hearingVenuePage } = this.requestHearingPageFactory;
    await hearingVenuePage.verifyContent(this.ccdCaseData);
    await hearingVenuePage.continue();
  }

  async addJudge() {
    const { hearingJudgePage } = this.requestHearingPageFactory;
    await hearingJudgePage.verifyContent(this.ccdCaseData);
    await hearingJudgePage.selectJudges();
    await hearingJudgePage.continue();
  }

  async addTiming() {
    const { hearingTimingPage } = this.requestHearingPageFactory;
    await hearingTimingPage.verifyContent(this.ccdCaseData);
    await hearingTimingPage.enterHearingLength();
    await hearingTimingPage.continue();
  }

  async enterAdditionalInstructions() {
    const { hearingAdditionalInstructionsPage } = this.requestHearingPageFactory;
    await hearingAdditionalInstructionsPage.verifyContent(this.ccdCaseData);
    await hearingAdditionalInstructionsPage.enterAdditionalInstructions();
    await hearingAdditionalInstructionsPage.continue();
  }

  async submitHearing() {
    const { hearingSubmitPage } = this.requestHearingPageFactory;
    await hearingSubmitPage.verifyContent(this.ccdCaseData);
    await hearingSubmitPage.continue();
  }

  async confirmHearing() {
    const { hearingConfirmPage } = this.requestHearingPageFactory;
    await hearingConfirmPage.verifyContent();
    await hearingConfirmPage.clickViewStatus();
  }

  async viewDetails() {
    const { caseDetailsPage } = this.requestHearingPageFactory;
    await caseDetailsPage.viewHearingDetails();
  }

  async editHearing() {
    const { hearingViewSummaryPage } = this.requestHearingPageFactory;
    await hearingViewSummaryPage.verifyContent();
    await hearingViewSummaryPage.editHearing();
  }

  async changeAdditionalFacilities() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.changeAdditionalFacilities();
  }

  async updateAdditionalFacilities() {
    const { hearingFacilitiesPage } = this.requestHearingPageFactory;
    await hearingFacilitiesPage.verifyContent(this.ccdCaseData);
    await hearingFacilitiesPage.updateAdditionalFacilities();
    await hearingFacilitiesPage.continue();
  }

  async changeJudgeTypes() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.changeJudgeTypes();
  }

  async updateJudgeTypes() {
    const { hearingJudgePage } = this.requestHearingPageFactory;
    await hearingJudgePage.verifyContent(this.ccdCaseData);
    await hearingJudgePage.updateJudges();
    await hearingJudgePage.continue();
  }

  async changeAttendance() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.changeAttendance();
  }

  async updateAttendance() {
    const { hearingAttendancePage } = this.requestHearingPageFactory;
    await hearingAttendancePage.verifyContent(this.ccdCaseData);
    await hearingAttendancePage.continue();
  }

  async changeTimings() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.changeTiming();
  }

  async updateTimings() {
    const { hearingTimingPage } = this.requestHearingPageFactory;
    await hearingTimingPage.verifyContent(this.ccdCaseData);
    await hearingTimingPage.updateHearingLength();
    await hearingTimingPage.continue();
  }

  async changeAdditionalInstructions() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.changeAdditionalInstructions();
  }

  async updateAdditionalInstructions() {
    const { hearingAdditionalInstructionsPage } = this.requestHearingPageFactory;
    await hearingAdditionalInstructionsPage.verifyContent(this.ccdCaseData);
    await hearingAdditionalInstructionsPage.enterUpdatedInstructions();
    await hearingAdditionalInstructionsPage.continue();
  }

  async submitUpdatedRequest() {
    const { hearingEditSummaryPage } = this.requestHearingPageFactory;
    await hearingEditSummaryPage.verifyContent(this.ccdCaseData);
    await hearingEditSummaryPage.continue();
  }

  async hearingChangeReason() {
    const { hearingChangeReasonPage } = this.requestHearingPageFactory;
    await hearingChangeReasonPage.verifyContent(this.ccdCaseData);
    await hearingChangeReasonPage.selectReasonPartyRequestedChange();
    await hearingChangeReasonPage.continue();
  }

  async cancelHearing() {
    const { caseDetailsPage } = this.requestHearingPageFactory;
    await caseDetailsPage.cancelHearing();
  }

  async cancelHearingListedInError() {
    const { hearingCancelPage } = this.requestHearingPageFactory;
    await hearingCancelPage.verifyContent();
    await hearingCancelPage.selectListedInError();
    await hearingCancelPage.continue();
  }
}
