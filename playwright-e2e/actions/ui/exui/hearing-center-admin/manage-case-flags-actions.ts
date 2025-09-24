import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-data';
import ManageCaseFlagsPageFactory from '../../../../pages/exui/hearing-center-admin/manage-case-flags/manage-case-flags-page-factory';

@AllMethodsStep()
export default class ManageCaseFlagsActions extends BaseTestData {
  private manageCaseFlagsPageFactory: ManageCaseFlagsPageFactory;

  constructor(manageCaseFlagsPageFactory: ManageCaseFlagsPageFactory, testData: TestData) {
    super(testData);
    this.manageCaseFlagsPageFactory = manageCaseFlagsPageFactory;
  }

  async makeInactiveCaseFlag() {
    const { manageCaseFlagsChooseFlagPage } = this.manageCaseFlagsPageFactory;
    await manageCaseFlagsChooseFlagPage.verifyContent(this.ccdCaseData, this.caseFlagsDetails);
    await manageCaseFlagsChooseFlagPage.selectFlag(this.firstActiveCaseFlagDetails);
    await manageCaseFlagsChooseFlagPage.submit();

    const { manageCaseFlagsUpdateFlagPage } = this.manageCaseFlagsPageFactory;
    await manageCaseFlagsUpdateFlagPage.verifyContent(
      this.ccdCaseData,
      this.firstActiveCaseFlagDetails,
    );
    await manageCaseFlagsUpdateFlagPage.clickMakeInactive();
    await manageCaseFlagsUpdateFlagPage.submit();

    const { submitManageCaseFlagsPage } = this.manageCaseFlagsPageFactory;
    await submitManageCaseFlagsPage.verifyContent(this.ccdCaseData);
    await submitManageCaseFlagsPage.submit();

    this.deactivateCaseFlag(0);
  }
}
