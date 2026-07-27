import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import ManageStayPageFactory from '../../../../pages/exui/hearing-center-admin/manage-stay/manage-stay-page-factory';

@AllMethodsStep()
export default class ManageStayActions extends BaseTestData {
  private manageStayPageFactory: ManageStayPageFactory;

  constructor(manageStayPageFactory: ManageStayPageFactory, testData: TestData) {
    super(testData);
    this.manageStayPageFactory = manageStayPageFactory;
  }

  async manageStayOptionsRequestUpdate() {
    const { manageStayOptionsPage } = this.manageStayPageFactory;
    await manageStayOptionsPage.verifyContent(this.ccdCaseData);
    await manageStayOptionsPage.selectRequestUpdate();
    await manageStayOptionsPage.submit();
  }

  async manageStayOptionsLiftStay() {
    const { manageStayOptionsPage } = this.manageStayPageFactory;
    await manageStayOptionsPage.verifyContent(this.ccdCaseData);
    await manageStayOptionsPage.selectLiftStay();
    await manageStayOptionsPage.submit();
  }

  async manageStayRequestUpdate() {
    const { manageStayRequestUpdatePage } = this.manageStayPageFactory;
    await manageStayRequestUpdatePage.verifyContent(this.ccdCaseData);
    await manageStayRequestUpdatePage.submit();
  }

  async manageStayLiftStayJudicialReferralInMediation() {
    const { manageStayJudicialReferralInMediationPage } = this.manageStayPageFactory;
    await manageStayJudicialReferralInMediationPage.verifyContent(this.ccdCaseData);
    await manageStayJudicialReferralInMediationPage.submit();
  }

  async submitManageStay() {
    const { submitManageStayPage } = this.manageStayPageFactory;
    await submitManageStayPage.verifyContent(this.ccdCaseData);
    await submitManageStayPage.submit();
  }

  async confirmManageStayRequestUpdate() {
    const { confirmManageStayRequestUpdatePage } = this.manageStayPageFactory;
    await confirmManageStayRequestUpdatePage.verifyContent(this.ccdCaseData);
    await confirmManageStayRequestUpdatePage.submit();
  }

  async confirmManageStayLiftStay() {
    const { confirmManageStayLiftStayPage } = this.manageStayPageFactory;
    await confirmManageStayLiftStayPage.verifyContent(this.ccdCaseData);
    await confirmManageStayLiftStayPage.submit();
  }
}
