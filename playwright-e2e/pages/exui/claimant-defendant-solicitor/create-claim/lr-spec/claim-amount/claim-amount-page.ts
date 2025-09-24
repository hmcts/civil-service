import BasePage from '../../../../../../base/base-page';
import partys from '../../../../../../constants/partys';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../../enums/claim-track';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import ExuiPage from '../../../../exui-page/exui-page';
import { paragraphs, subheadings, inputs } from './claim-amount-content';

@AllMethodsStep()
export default class ClaimAmountPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.claimAmount),
      super.expectText(paragraphs.descriptionText),
    ]);
  }

  async addNew() {
    await super.clickAddNew();
  }

  async enterClaimDetailsSmallTrack() {
    const claimAmount = CaseDataHelper.getClaimValue(ClaimTrack.SMALL_CLAIM);
    await super.inputText(
      `Roof damage - ${partys.CLAIMANT_1.key}`,
      inputs.claim.reason.selector(1),
    );
    await super.inputText(claimAmount, inputs.claim.amount.selector(1));
  }

  async enterClaimDetailsFastTrack() {
    const claimAmount = CaseDataHelper.getClaimValue(ClaimTrack.FAST_CLAIM);
    await super.inputText(
      `Roof damage - ${partys.CLAIMANT_1.key}`,
      inputs.claim.reason.selector(1),
    );
    await super.inputText(claimAmount, inputs.claim.amount.selector(1));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
