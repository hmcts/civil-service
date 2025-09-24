import BaseTestData from '../../../../base/base-test-data.ts';
import { AllMethodsStep } from '../../../../decorators/test-steps.ts';
import TestData from '../../../../models/test-data.ts';
import CaseProceedsInCasemanPageFactory from '../../../../pages/exui/caseworker/case-proceeds-in-caseman/case-proceeds-in-caseman-page-factory.ts';

@AllMethodsStep()
export default class CaseProceedsInCasemanActions extends BaseTestData {
  private caseProceedsInCasemanPageFactory: CaseProceedsInCasemanPageFactory;

  constructor(
    caseProceedsInCasemanPageFactory: CaseProceedsInCasemanPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.caseProceedsInCasemanPageFactory = caseProceedsInCasemanPageFactory;
  }

  async caseSettled() {
    const { caseProceedsInCasemanPage } = this.caseProceedsInCasemanPageFactory;
    await caseProceedsInCasemanPage.verifyContent(this.ccdCaseData);
    await caseProceedsInCasemanPage.enterTodayDate();
    await caseProceedsInCasemanPage.selectCaseSettled();
    await caseProceedsInCasemanPage.submit();
  }

  async caseSettledSpec() {
    const { caseProceedsInCasemanLRPage } = this.caseProceedsInCasemanPageFactory;
    await caseProceedsInCasemanLRPage.verifyContent(this.ccdCaseData);
    await caseProceedsInCasemanLRPage.enterTodayDate();
    await caseProceedsInCasemanLRPage.selectCaseSettled();
    await caseProceedsInCasemanLRPage.submit();
  }
}
