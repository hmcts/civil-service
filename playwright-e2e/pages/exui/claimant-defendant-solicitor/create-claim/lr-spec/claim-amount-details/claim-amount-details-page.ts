import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ClaimTrack from '../../../../../../enums/claim-track';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import ExuiPage from '../../../../exui-page/exui-page';
import { tableHeadings, tableRowNames } from './claim-amount-details-content';

@AllMethodsStep()
export default class ClaimAmountDetailsPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectText(tableHeadings.description),
      super.expectText(tableHeadings.amount),
    ]);
  }

  async verifySmallTrack() {
    const amount = `£ ${CaseDataHelper.getClaimValue(ClaimTrack.SMALL_CLAIM).toFixed(2)}`;
    await super.expectTableValueByRowName(tableRowNames.total, amount);
  }

  async verifyFastTrack() {
    const amount = `£ ${CaseDataHelper.getClaimValue(ClaimTrack.FAST_CLAIM).toFixed(2)}`;
    await super.expectTableValueByRowName(tableRowNames.total, amount);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
