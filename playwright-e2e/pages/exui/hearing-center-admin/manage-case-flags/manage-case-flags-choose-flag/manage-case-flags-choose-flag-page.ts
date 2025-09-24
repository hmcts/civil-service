import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { CaseFlagDetails } from '../../../../../models/case-flag';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import { getFormattedCaseId } from '../../../exui-page/exui-content';
import ExuiPage from '../../../exui-page/exui-page';
import { heading } from './manage-case-flags-choose-flag-content';

@AllMethodsStep()
export default class ManageCaseFlagsChooseFlagPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData, caseFlagsDetails: CaseFlagDetails[]) {
    await super.runVerifications([
      super.expectHeading(getFormattedCaseId(ccdCaseData.id), { exact: false }),
      super.expectHeading(ccdCaseData.caseNamePublic, { exact: false }),
      super.expectHeading(heading, { count: 2 }),
      ...caseFlagsDetails.map((caseFlagDetails) =>
        super.expectLabel(caseFlagDetails.caseFlagComment, {
          ignoreDuplicates: true,
          exact: false,
        }),
      ),
    ]);
  }

  async selectFlag(caseFlagDetails: CaseFlagDetails) {
    await super.clickByLabel(caseFlagDetails.caseFlagComment, { first: true, exact: false });
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
