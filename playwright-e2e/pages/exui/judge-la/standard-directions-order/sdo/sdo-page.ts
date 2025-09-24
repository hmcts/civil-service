import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { inputs, radioButtons } from './sdo-content';

@AllMethodsStep()
export default class SdoPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.drawDirectionsOrder.label),
      super.expectRadioYesLabel(radioButtons.drawDirectionsOrder.yes.selector),
      super.expectRadioNoLabel(radioButtons.drawDirectionsOrder.no.selector),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.drawDirectionsOrder.yes.selector);
    await super.expectLabel(inputs.judgementSum.label);
    await super.inputText('100', inputs.judgementSum.selector);
  }

  async selectNo() {
    await super.clickBySelector(radioButtons.drawDirectionsOrder.no.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
