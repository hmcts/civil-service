import BaseTestData from '../../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import TestData from '../../../../../models/test-utils/test-data';
import SetAsideJudgmentPageFactory from '../../../../../pages/exui/hearing-center-admin/set-aside-judgment/set-aside-judgment-page-factory';

@AllMethodsStep()
export default class SetAsideJudgmentSpecActions extends BaseTestData {
  private setAsideJudgmentPageFactory: SetAsideJudgmentPageFactory;

  constructor(setAsideJudgmentPageFactory: SetAsideJudgmentPageFactory, testData: TestData) {
    super(testData);
    this.setAsideJudgmentPageFactory = setAsideJudgmentPageFactory;
  }

  async setAsideJudgment() {
    const { setAsideJudgmentPage } = this.setAsideJudgmentPageFactory;
    await setAsideJudgmentPage.verifyContent(this.ccdCaseData);
    await setAsideJudgmentPage.selectJudgeOrder();
    await setAsideJudgmentPage.submit();
  }

  async setAsideOrderFollowingApplication() {
    const { setAsideOrderTypePage } = this.setAsideJudgmentPageFactory;
    await setAsideOrderTypePage.verifyContent(this.ccdCaseData);
    await setAsideOrderTypePage.fillOrderFollowingApplication();
    await setAsideOrderTypePage.submit();
  }

  async setAsideOrderFollowingDefenceReceived() {
    const { setAsideOrderTypePage } = this.setAsideJudgmentPageFactory;
    await setAsideOrderTypePage.verifyContent(this.ccdCaseData);
    await setAsideOrderTypePage.fillOrderFollowingDefenceReceived();
    await setAsideOrderTypePage.submit();
  }

  async setAsideJudgmentMadeInError() {
    const { setAsideJudgmentPage } = this.setAsideJudgmentPageFactory;
    await setAsideJudgmentPage.verifyContent(this.ccdCaseData);
    await setAsideJudgmentPage.selectJudgmentError();
    await setAsideJudgmentPage.submit();
  }

  async submitSetAsideJudgment() {
    const { submitSetAsideJudgmentPage } = this.setAsideJudgmentPageFactory;
    await submitSetAsideJudgmentPage.verifyContent(this.ccdCaseData);
    await submitSetAsideJudgmentPage.submit();
  }

  async confirmSetAsideJudgment() {
    const { confirmSetAsideJudgmentPage } = this.setAsideJudgmentPageFactory;
    await confirmSetAsideJudgmentPage.verifyContent(this.ccdCaseData);
    await confirmSetAsideJudgmentPage.submit();
  }
}
