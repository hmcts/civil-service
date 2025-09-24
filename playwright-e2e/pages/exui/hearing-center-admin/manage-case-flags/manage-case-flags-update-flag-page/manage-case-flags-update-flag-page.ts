import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import { CaseFlagDetails } from '../../../../../models/case-flag';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { buttons, heading, inputs } from './manage-case-flags-update-flag-content';

@AllMethodsStep()
export default class ManageCaseFlagsUpdateFlagPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData, caseFlagDetails: CaseFlagDetails) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading(caseFlagDetails.caseFlagType)),
      super.expectLabel(inputs.flagComment.label(caseFlagDetails.caseFlagType)),
      super.expectButton(buttons.makeInactive.label),
    ]);
  }

  async clickMakeInactive() {
    await super.clickButtonByName(buttons.makeInactive.label);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
