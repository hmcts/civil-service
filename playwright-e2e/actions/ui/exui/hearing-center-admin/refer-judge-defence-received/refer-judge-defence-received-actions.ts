import BaseTestData from '../../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import TestData from '../../../../../models/test-utils/test-data';
import ReferJudgeDefenceReceivedPageFactory from '../../../../../pages/exui/hearing-center-admin/refer-to-judge-defended-claim/refer-to-judge-defended-claim-page-factory';

@AllMethodsStep()
export default class ReferJudgeDefenceReceivedActions extends BaseTestData {
  private referJudgeDefenceReceivedPageFactory: ReferJudgeDefenceReceivedPageFactory;

  constructor(
    referJudgeDefenceReceivedPageFactory: ReferJudgeDefenceReceivedPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.referJudgeDefenceReceivedPageFactory = referJudgeDefenceReceivedPageFactory;
  }

  async referToJudge() {
    const { referJudgeDefenceReceivedPage } = this.referJudgeDefenceReceivedPageFactory;
    await referJudgeDefenceReceivedPage.verifyContent(this.ccdCaseData);
    await referJudgeDefenceReceivedPage.selectConfirm();
    await referJudgeDefenceReceivedPage.submit();
  }

  async confirmReferToJudge() {
    const { confirmReferJudgeDefenceReceivedPage } = this.referJudgeDefenceReceivedPageFactory;
    await confirmReferJudgeDefenceReceivedPage.verifyContent(this.ccdCaseData);
    await confirmReferJudgeDefenceReceivedPage.submit();
  }
}
