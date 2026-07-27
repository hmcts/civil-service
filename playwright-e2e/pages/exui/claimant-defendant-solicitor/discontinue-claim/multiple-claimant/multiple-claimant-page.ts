import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { getFormattedCaseId } from '../../../mixin-pages/exui-page/exui-content';
import { headings, radioButtons } from './multiple-claimant-content';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';

@AllMethodsStep()
export default class MultipleClaimantPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData, claimant1PartyType: ClaimantDefendantPartyType, claimant2PartyType: ClaimantDefendantPartyType) {
    await super.runVerifications([
      super.expectText(headings.discontinueThisClaim),
      super.expectHeading(headings.whoIsDiscontinuing),
      super.expectHeading(getFormattedCaseId(ccdCaseData?.id!), { exact: false }),
      super.expectHeading(ccdCaseData?.caseNamePublic!, { exact: false }),
      super.expectLabel(radioButtons.claimantWhoIsDiscontinuing.claimant1.label(claimant1PartyType)),
      super.expectLabel(radioButtons.claimantWhoIsDiscontinuing.claimant2.label(claimant2PartyType)),
    ]);
  }

  async selectBoth() {
    await super.clickByLabel(radioButtons.claimantWhoIsDiscontinuing.both.label);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
