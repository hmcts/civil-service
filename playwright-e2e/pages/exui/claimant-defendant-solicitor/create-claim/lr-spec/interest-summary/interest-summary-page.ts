import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../../enums/claim-track';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import ExuiPage from '../../../../exui-page/exui-page';
import { subheadings, tableHeaders, tableRowNames } from './interest-summary-content';

@AllMethodsStep()
export default class InterestSummaryPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectText(subheadings.totalAmountOfClaim),
      super.expectText(tableHeaders.description),
      super.expectText(tableHeaders.amount),
    ]);
  }

  async verifySmallTrack() {
    const amount = `£ ${CaseDataHelper.getClaimValue(ClaimTrack.SMALL_CLAIM).toFixed(2)}`;
    await super.expectTableValueByRowName(tableRowNames.claimAmount, amount);
    await super.expectTableValueByRowName(tableRowNames.totalAmount, amount);
  }

  async verifyFastTrack() {
    const amount = `£ ${CaseDataHelper.getClaimValue(ClaimTrack.FAST_CLAIM).toFixed(2)}`;
    await super.expectTableValueByRowName(tableRowNames.claimAmount, amount);
    await super.expectTableValueByRowName(tableRowNames.totalAmount, amount);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
