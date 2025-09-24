import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, inputs } from './create-case-flags-reasonable-adjustment-content';
import ReasonableAdjustmentFlags from '../../../../../../enums/case-flags/reasonable-adjustment-flags';

@AllMethodsStep()
export default class CreateCaseFlagsReasonableAdjustmentPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(ReasonableAdjustmentFlags.ALTERNATIVE_DOCUMENTS),
      super.expectLabel(ReasonableAdjustmentFlags.FORMS),
      super.expectLabel(ReasonableAdjustmentFlags.BUILDING_ACCESS),
      super.expectLabel(ReasonableAdjustmentFlags.HEARING_SUPPORT),
      super.expectLabel(ReasonableAdjustmentFlags.COMFORT_DURING_HEARING),
      super.expectLabel(ReasonableAdjustmentFlags.HEARING_REQUEST),
      super.expectLabel(ReasonableAdjustmentFlags.COMMUNICATING),
      super.expectLabel(ReasonableAdjustmentFlags.OTHER),
    ]);
  }

  async selectFlag(caseflag: ReasonableAdjustmentFlags) {
    await super.clickByLabel(caseflag);
    if (caseflag === ReasonableAdjustmentFlags.OTHER) {
      await super.inputText(ReasonableAdjustmentFlags.OTHER, inputs.other.selector);
    }
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
