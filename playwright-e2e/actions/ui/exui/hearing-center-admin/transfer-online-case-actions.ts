import TestData from '../../../../models/test-utils/test-data.ts';
import { Step } from '../../../../decorators/test-steps.ts';
import BaseTestData from '../../../../base/base-test-data.ts';
import TransferOnlineCasePageFactory from '../../../../pages/exui/hearing-center-admin/transfer-online-case/transfer-online-case-page-factory.ts';

const classKey = 'TransferOnlineCaseActions';
export default class TransferOnlineCaseActions extends BaseTestData {
  private transferOnlineCasePageFactory: TransferOnlineCasePageFactory;

  constructor(transferOnlineCasePageFactory: TransferOnlineCasePageFactory, testData: TestData) {
    super(testData);
    this.transferOnlineCasePageFactory = transferOnlineCasePageFactory;
  }

  @Step(classKey)
  async transferOnlineCase() {
    const { transferOnlineCasePage } = this.transferOnlineCasePageFactory;
    await transferOnlineCasePage.verifyContent();
    await transferOnlineCasePage.selectCourt();
    await transferOnlineCasePage.enterReason();
    await transferOnlineCasePage.submit();
  }

  @Step(classKey)
  async submitTransferOnlineCase() {
    const { transferOnlineCaseSubmitPage } = this.transferOnlineCasePageFactory;
    await transferOnlineCaseSubmitPage.verifyContent();
    await transferOnlineCaseSubmitPage.submit();
  }

  @Step(classKey)
  async confirm() {
    const { transferOnlineCaseConfirmPage } = this.transferOnlineCasePageFactory;
    await transferOnlineCaseConfirmPage.verifyContent();
  }
}
