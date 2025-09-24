import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, inputs } from './create-case-flags-special-measure-content';
import SpecialMeasureFlags from '../../../../../../enums/case-flags/special-measure-flags';

@AllMethodsStep()
export default class CreateCaseFlagsSpecialMeasurePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(SpecialMeasureFlags.SCREENING_WITNESS),
      super.expectLabel(SpecialMeasureFlags.EVIDENCE_BY_LINK),
      super.expectLabel(SpecialMeasureFlags.OTHER),
    ]);
  }

  async selectFlag(caseflag: SpecialMeasureFlags) {
    await super.clickByLabel(caseflag);
    if (caseflag === SpecialMeasureFlags.OTHER) {
      await super.inputText(SpecialMeasureFlags.OTHER, inputs.other.selector);
    }
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
