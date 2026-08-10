import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { inputs, radioButtons } from './not-suitable-sdo-content';

@AllMethodsStep()
export default class NotSuitableSdoPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.reason.label),
      super.expectRadioLabel(
        radioButtons.reason.transferCase.label,
        radioButtons.reason.transferCase.selector,
      ),
      super.expectRadioLabel(
        radioButtons.reason.otherReason.label,
        radioButtons.reason.otherReason.selector,
      ),
    ]);
  }

  async selectTransferCase() {
    await super.clickBySelector(radioButtons.reason.transferCase.selector);
    await super.expectLabel(inputs.transferCase.label);
    await super.expectText(`${inputs.transferCase.paragraph1} ${inputs.transferCase.paragraph2}`);
    await super.inputText('Details here', inputs.transferCase.selector);
  }

  async selectOtherReason() {
    await super.clickBySelector(radioButtons.reason.otherReason.selector);
    await super.expectLabel(inputs.otherReason.label);
    await super.expectText(
      `${inputs.otherReason.paragraph1} ${inputs.otherReason.paragraph2} ${inputs.otherReason.paragraph3}`,
    );
    await super.inputText('Other reason here', inputs.otherReason.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
