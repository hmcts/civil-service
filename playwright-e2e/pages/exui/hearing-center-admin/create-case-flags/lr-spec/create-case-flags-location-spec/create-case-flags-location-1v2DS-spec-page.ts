import BasePage from '../../../../../../base/base-page';
import caseFlagLocations from '../../../../../../constants/case-flags/case-flag-locations';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import { CaseFlagLocation } from '../../../../../../models/case-flags/case-flag-locations';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types';
import ExuiPage from '../../../../exui-page/exui-page';

@AllMethodsStep()
export default class CreateCaseFlagsLocation1v2DSSpecPage extends ExuiPage(BasePage) {
  async verifyContent(
    ccdCaseData: CCDCaseData,
    claimant1PartyType: ClaimantDefendantPartyType,
    defendant1PartyType: ClaimantDefendantPartyType,
    defendant2PartyType: ClaimantDefendantPartyType,
  ) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(caseFlagLocations.CASE_LEVEL),
      super.expectLabel(caseFlagLocations.CLAIMANT_1(claimant1PartyType)),
      super.expectLabel(caseFlagLocations.DEFENDANT_1(defendant1PartyType)),
      super.expectLabel(caseFlagLocations.DEFENDANT_2(defendant2PartyType)),
      super.expectLabel(caseFlagLocations.CLAIMANT_EXPERT_1),
      super.expectLabel(caseFlagLocations.CLAIMANT_WITNESS_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_1_WITNESS_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_1_EXPERT_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_2_WITNESS_1),
      super.expectLabel(caseFlagLocations.DEFENDANT_2_EXPERT_1),
    ]);
  }

  async selectLocation(caseFlagLocation: CaseFlagLocation) {
    await super.clickByLabel(caseFlagLocation);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
