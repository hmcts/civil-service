import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import SdoDJPageFactory from '../../../../pages/exui/judge-la/sdo-dj/sdo-dj-page-factory';

@AllMethodsStep()
export default class SdoDJActions extends BaseTestData {
  private sdoDJPageFactory: SdoDJPageFactory;

  constructor(sdoDJPageFactory: SdoDJPageFactory, testData: TestData) {
    super(testData);
    this.sdoDJPageFactory = sdoDJPageFactory;
  }

  async sdoDJSelectDisposalHearing() {
    const { caseManagementOrderPage } = this.sdoDJPageFactory;
    await caseManagementOrderPage.verifyContent(this.ccdCaseData);
    await caseManagementOrderPage.selectDisposalHearing();
    await caseManagementOrderPage.submit();
  }

  async sdoDJSelectTrialHearing() {
    const { caseManagementOrderPage } = this.sdoDJPageFactory;
    await caseManagementOrderPage.verifyContent(this.ccdCaseData);
    await caseManagementOrderPage.selectTrialHearing();
    await caseManagementOrderPage.submit();
  }

  async sdoDJDisposalHearingDetails() {
    const { disposalHearingSdoDJPage } = this.sdoDJPageFactory;
    await disposalHearingSdoDJPage.verifyContent(this.ccdCaseData);
    await disposalHearingSdoDJPage.addHearingTimeEstimate();
    await disposalHearingSdoDJPage.submit();
  }

  async sdoDJTrialHearingDetails() {
    const { trialHearingSdoDJPage } = this.sdoDJPageFactory;
    await trialHearingSdoDJPage.verifyContent(this.ccdCaseData);
    await trialHearingSdoDJPage.addHearingTimeEstimate();
    await trialHearingSdoDJPage.submit();
  }

  async sdoDJOrderPreview() {
    const { orderPreviewSdoDJPage } = this.sdoDJPageFactory;
    await orderPreviewSdoDJPage.verifyContent(this.ccdCaseData);
    await orderPreviewSdoDJPage.submit();
  }

  async sdoDJSubmit() {
    const { submitSdoDJPage } = this.sdoDJPageFactory;
    await submitSdoDJPage.verifyContent(this.ccdCaseData);
    await submitSdoDJPage.submit();
  }

  async sdoDJConfirm() {
    const { confirmSdoDJPage } = this.sdoDJPageFactory;
    await confirmSdoDJPage.verifyContent(this.ccdCaseData);
    await confirmSdoDJPage.submit();
  }
}
