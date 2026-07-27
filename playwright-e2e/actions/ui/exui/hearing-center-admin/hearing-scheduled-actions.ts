import BaseTestData from '../../../../base/base-test-data';
import { Step } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import HearingScheduledPageFactory from '../../../../pages/exui/hearing-center-admin/hearing-scheduled/hearing-scheduled-page-factory';

const classKey = 'HearingScheduledActions';
export default class HearingScheduledActions extends BaseTestData {
  private hearingScheduledPageFactory: HearingScheduledPageFactory;

  constructor(hearingScheduledPageFactory: HearingScheduledPageFactory, testData: TestData) {
    super(testData);
    this.hearingScheduledPageFactory = hearingScheduledPageFactory;
  }

  @Step(classKey)
  async hearingNoticeSmallClaim() {
    const { hearingNoticeSelectPage } = this.hearingScheduledPageFactory;
    await hearingNoticeSelectPage.verifyContent();
    await hearingNoticeSelectPage.selectSmallClaims();
    await hearingNoticeSelectPage.submit();
  }

  @Step(classKey)
  async hearingNoticeTrail() {
    const { hearingNoticeSelectPage } = this.hearingScheduledPageFactory;
    await hearingNoticeSelectPage.verifyContent();
    await hearingNoticeSelectPage.selectTrail();
    await hearingNoticeSelectPage.submit();
  }

  @Step(classKey)
  async listingOrRelisting() {
    const { listingOrRelistingPage } = this.hearingScheduledPageFactory;
    await listingOrRelistingPage.verifyContent();
    await listingOrRelistingPage.selectListing();
    await listingOrRelistingPage.submit();
  }

  @Step(classKey)
  async hearingDetails() {
    const { hearingDetailsPage } = this.hearingScheduledPageFactory;
    await hearingDetailsPage.verifyContent();
    await hearingDetailsPage.enterHearingDetails();
    await hearingDetailsPage.submit();
  }

  @Step(classKey)
  async hearingInformation() {
    const { hearingInformationPage } = this.hearingScheduledPageFactory;
    await hearingInformationPage.verifyContent();
    await hearingInformationPage.enterMoreInformation();
    await hearingInformationPage.submit();
  }

  @Step(classKey)
  async submitHearingScheduled() {
    const { hearingScheduledSubmitPage } = this.hearingScheduledPageFactory;
    await hearingScheduledSubmitPage.verifyContent();
    await hearingScheduledSubmitPage.submit();
  }

  @Step(classKey)
  async confirm() {
    const { hearingScheduledConfirmPage } = this.hearingScheduledPageFactory;
    await hearingScheduledConfirmPage.verifyContent();
  }
}
