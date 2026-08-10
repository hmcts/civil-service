import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { heading, radioButtons } from './multiple-claimants-content';

@AllMethodsStep()
export default class MultipleClaimantsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(radioButtons.markPaidForAllClaimants.yes.label),
      super.expectLabel(radioButtons.markPaidForAllClaimants.no.label),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.markPaidForAllClaimants.yes.selector);
  }

  async selectNo(claimant2PartyType: ClaimantDefendantPartyType) {
    await super.clickBySelector(radioButtons.markPaidForAllClaimants.no.selector);
    await super.clickByLabel(radioButtons.claimantRelatesTo.claimant2.label(claimant2PartyType));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
