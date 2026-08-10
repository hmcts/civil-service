import BasePage from '../../../../../base/base-page';
import ExuiHearingsPage from '../../../mixin-pages/exui-hearings-page/exui-hearings-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { heading, inputs } from './hearing-additional-instructions-content';

@AllMethodsStep()
export default class HearingAdditionalInstructionsPage extends ExuiHearingsPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyCaseName(ccdCaseData),
      super.expectHeading(heading),
    ]);
  }

  async enterAdditionalInstructions() {
    await super.inputText(
      inputs.additionalInstructions.initialText,
      inputs.additionalInstructions.selector,
    );
  }

  async enterUpdatedInstructions() {
    await super.inputText(
      inputs.additionalInstructions.updatedText,
      inputs.additionalInstructions.selector,
    );
  }

  async continue() {
    await super.clickContinue();
  }
}
