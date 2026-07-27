import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import DecisionOnReconsiderationRequestPageFactory from '../../../../pages/exui/judge-la/decision-on-reconsideration-request/decision-on-reconsideration-request-page-factory';

@AllMethodsStep()
export default class DecisionOnReconsiderationRequestActions extends BaseTestData {
  private decisionOnReconsiderationRequestPageFactory: DecisionOnReconsiderationRequestPageFactory;

  constructor(
    decisionOnReconsiderationRequestPageFactory: DecisionOnReconsiderationRequestPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.decisionOnReconsiderationRequestPageFactory = decisionOnReconsiderationRequestPageFactory;
  }

  async selectYes() {
    const { judgeResponseToReconsiderationPage } = this.decisionOnReconsiderationRequestPageFactory;
    await judgeResponseToReconsiderationPage.verifyContent(this.ccdCaseData);
    await judgeResponseToReconsiderationPage.selectYes();
    await judgeResponseToReconsiderationPage.submit();
  }

  async selectNoCreateNewSdo() {
    const { judgeResponseToReconsiderationPage } = this.decisionOnReconsiderationRequestPageFactory;
    await judgeResponseToReconsiderationPage.verifyContent(this.ccdCaseData);
    await judgeResponseToReconsiderationPage.selectNoCreateNewSdo();
    await judgeResponseToReconsiderationPage.submit();
  }

  async selectNoPreviousOrderNeedsAmending() {
    const { judgeResponseToReconsiderationPage } = this.decisionOnReconsiderationRequestPageFactory;
    await judgeResponseToReconsiderationPage.verifyContent(this.ccdCaseData);
    await judgeResponseToReconsiderationPage.selectNoCreateGeneralOrder();
    await judgeResponseToReconsiderationPage.submit();
  }

  async orderPreview() {
    const { orderPreviewDecisionOnReconsiderationRequestPage } =
      this.decisionOnReconsiderationRequestPageFactory;
    await orderPreviewDecisionOnReconsiderationRequestPage.verifyContent(this.ccdCaseData);
    await orderPreviewDecisionOnReconsiderationRequestPage.submit();
  }

  async submitDecisionOnReconsideration() {
    const { submitDecisionOnReconsiderationRequestPage } =
      this.decisionOnReconsiderationRequestPageFactory;
    await submitDecisionOnReconsiderationRequestPage.verifyContent();
    await submitDecisionOnReconsiderationRequestPage.submit();
  }

  async confirmDecisionOnReconsiderationRequestUpholdOrder() {
    const { confirmDecisionOnReconsiderationRequestUpholdOrderPage } =
      this.decisionOnReconsiderationRequestPageFactory;
    await confirmDecisionOnReconsiderationRequestUpholdOrderPage.verifyContent(this.ccdCaseData);
    await confirmDecisionOnReconsiderationRequestUpholdOrderPage.submit();
  }

  async confirmDecisionOnReconsiderationRequestCreateSdo() {
    const { confirmDecisionOnReconsiderationRequestCreateSdoPage } =
      this.decisionOnReconsiderationRequestPageFactory;
    await confirmDecisionOnReconsiderationRequestCreateSdoPage.verifyContent(this.ccdCaseData);
    await confirmDecisionOnReconsiderationRequestCreateSdoPage.submit();
  }

  async confirmDecisionOnReconsiderationRequestCreateGeneralOrder() {
    const { confirmDecisionOnReconsiderationRequestCreateGeneralOrderPage } =
      this.decisionOnReconsiderationRequestPageFactory;
    await confirmDecisionOnReconsiderationRequestCreateGeneralOrderPage.verifyContent(
      this.ccdCaseData,
    );
    await confirmDecisionOnReconsiderationRequestCreateGeneralOrderPage.submit();
  }
}
