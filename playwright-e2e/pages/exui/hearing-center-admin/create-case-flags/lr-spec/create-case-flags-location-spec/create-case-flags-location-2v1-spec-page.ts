import BasePage from '../../../../../../base/base-page';
import caseFlagLocations from '../../../../../../constants/ccd-events/case-flags/case-flag-locations';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import { CaseFlagLocation } from '../../../../../../models/ccd-events/case-flags/case-flag-locations';
import CCDCaseData from '../../../../../../models/ccd-case-data';
import { ClaimantDefendantPartyType } from '../../../../../../models/users/claimant-defendant-party-types';
import ExuiPage from '../../../../mixin-pages/exui-page/exui-page';

@AllMethodsStep()
export default class CreateCaseFlagsLocation2v1SpecPage extends ExuiPage(BasePage) {
  async verifyContent(
    ccdCaseData: CCDCaseData,
    claimant1PartyType: ClaimantDefendantPartyType,
    claimant2PartyType: ClaimantDefendantPartyType,
    defendant1PartyType: ClaimantDefendantPartyType,
  ) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(caseFlagLocations.CASE_LEVEL),
      super.expectLabel(caseFlagLocations.CLAIMANT_1(claimant1PartyType)),
      super.expectLabel(caseFlagLocations.CLAIMANT_2(claimant2PartyType)),
      super.expectLabel(caseFlagLocations.DEFENDANT_1(defendant1PartyType)),
      super.expectLabel(caseFlagLocations.CLAIMANT_EXPERT_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_1_EXPERT_1),
      super.expectLabel(caseFlagLocations.CLAIMANT_WITNESS_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_1_WITNESS_1),
    ]);
  }

  async selectLocation(caseFlagLocation: CaseFlagLocation) {
    await super.clickByLabel(caseFlagLocation);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
