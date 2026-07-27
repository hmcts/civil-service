import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import RequestForReconsiderationPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/request-for-reconsideration/request-for-reconsideration-page-factory';

@AllMethodsStep()
export default class RequestForReconsiderationActions extends BaseTestData {
  private requestForReconsiderationPageFactory: RequestForReconsiderationPageFactory;

  constructor(
    requestForReconsiderationPageFactory: RequestForReconsiderationPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.requestForReconsiderationPageFactory = requestForReconsiderationPageFactory;
  }

  async enterReason() {
    const { requestForReconsiderationPage } = this.requestForReconsiderationPageFactory;
    await requestForReconsiderationPage.verifyContent(this.ccdCaseData);
    await requestForReconsiderationPage.enterReason();
    await requestForReconsiderationPage.submit();
  }

  async submitRequestForReconsideration() {
    const { submitRequestForReconsiderationPage } = this.requestForReconsiderationPageFactory;
    await submitRequestForReconsiderationPage.verifyContent();
    await submitRequestForReconsiderationPage.submit();
  }

  async confirmRequestForReconsideration() {
    const { confirmRequestForReconsiderationPage } = this.requestForReconsiderationPageFactory;
    await confirmRequestForReconsiderationPage.verifyContent(this.ccdCaseData);
    await confirmRequestForReconsiderationPage.submit();
  }
}
