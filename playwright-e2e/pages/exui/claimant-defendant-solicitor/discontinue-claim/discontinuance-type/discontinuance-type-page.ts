import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { headings, radioButtons } from './discontinuance-type-content';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';

@AllMethodsStep()
export default class DiscontinuanceTypePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectText(headings.discontinueThisClaim),
      super.expectHeading(headings.typeOfDiscontinuance),
      super.expectHeading(getFormattedCaseId(ccdCaseData?.id!), { exact: false }),
      super.expectRadioLabel(
        radioButtons.fullDiscontinuance.label,
        radioButtons.fullDiscontinuance.selector,
      ),
      super.expectRadioLabel(
        radioButtons.partDiscontinuance.label,
        radioButtons.partDiscontinuance.selector,
      ),
    ]);
  }

  async selectFullDiscontinuance() {
    await super.clickBySelector(radioButtons.fullDiscontinuance.selector);
  }

  async selectPartDiscontinuance() {
    await super.clickBySelector(radioButtons.partDiscontinuance.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
