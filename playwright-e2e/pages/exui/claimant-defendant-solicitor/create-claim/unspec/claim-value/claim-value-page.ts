import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../../enums/claim-track';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import ExuiPage from '../../../../exui-page/exui-page';
import { inputs, paragraphs, subheadings } from './claim-value-content';

@AllMethodsStep()
export default class ClaimValuePage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectText(subheadings.claimValue),
      super.expectText(paragraphs.descriptionText),
      super.expectLabel(inputs.amount.label),
    ]);
  }

  async enterClaimDetailsSmallTrack() {
    const claimAmount = CaseDataHelper.getClaimValue(ClaimTrack.SMALL_CLAIM);
    await super.inputText(claimAmount, inputs.amount.selector);
  }

  async enterClaimDetailsFastTrack() {
    const claimAmount = CaseDataHelper.getClaimValue(ClaimTrack.FAST_CLAIM);
    await super.inputText(claimAmount, inputs.amount.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
