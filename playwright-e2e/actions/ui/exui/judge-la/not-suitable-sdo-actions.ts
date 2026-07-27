import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import NotSuitableSdoPageFactory from '../../../../pages/exui/judge-la/not-suitable-for-sdo/not-suitable-sdo-page-factory';

@AllMethodsStep()
export default class NotSuitableSdoActions extends BaseTestData {
  private notSuitableSdoPageFactory: NotSuitableSdoPageFactory;

  constructor(
    notSuitableSdoPageFactory: NotSuitableSdoPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.notSuitableSdoPageFactory = notSuitableSdoPageFactory;
  }

  async selectTransferCase() {
    const { notSuitableSdoPage } = this.notSuitableSdoPageFactory;
    await notSuitableSdoPage.verifyContent(this.ccdCaseData);
    await notSuitableSdoPage.selectTransferCase();
    await notSuitableSdoPage.submit();
  }

  async selectOtherReason() {
    const { notSuitableSdoPage } = this.notSuitableSdoPageFactory;
    await notSuitableSdoPage.verifyContent(this.ccdCaseData);
    await notSuitableSdoPage.selectOtherReason();
    await notSuitableSdoPage.submit();
  }

  async submitNotSuitableSdo() {
    const { submitNotSuitableSdoPage } = this.notSuitableSdoPageFactory;
    await submitNotSuitableSdoPage.verifyContent(this.ccdCaseData);
    await submitNotSuitableSdoPage.submit();
  }

  async confirmOtherReasonNotSuitableSdo() {
    const { confirmOtherReasonNotSuitableSdoPage } = this.notSuitableSdoPageFactory;
    await confirmOtherReasonNotSuitableSdoPage.verifyContent(this.ccdCaseData);
    await confirmOtherReasonNotSuitableSdoPage.submit();
  }

  async confirmTransferCaseNotSuitableSdo() {
    const { confirmTransferCaseNotSuitableSdoPage } = this.notSuitableSdoPageFactory;
    await confirmTransferCaseNotSuitableSdoPage.verifyContent(this.ccdCaseData);
    await confirmTransferCaseNotSuitableSdoPage.submit();
  }
}
