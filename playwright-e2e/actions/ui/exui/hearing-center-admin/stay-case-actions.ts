import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import StayCasePageFactory from '../../../../pages/exui/hearing-center-admin/stay-case/stay-case-page-factory';

@AllMethodsStep()
export default class StayCaseActions extends BaseTestData {
  private stayCasePageFactory: StayCasePageFactory;

  constructor(stayCasePageFactory: StayCasePageFactory, testData: TestData) {
    super(testData);
    this.stayCasePageFactory = stayCasePageFactory;
  }

  async stayCase() {
    const { stayCasePage } = this.stayCasePageFactory;
    await stayCasePage.verifyContent(this.ccdCaseData);
    await stayCasePage.submit();
  }

  async confirmStayCase() {
    const { confirmStayCasePage } = this.stayCasePageFactory;
    await confirmStayCasePage.verifyContent(this.ccdCaseData);
    await confirmStayCasePage.submit();
  }
}
