import BasePage from '../../../../../base/base-page';
import partys from '../../../../../constants/partys';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import { ClaimantDefendantPartyType } from '../../../../../models/claimant-defendant-party-types';
import ExuiPage from '../../../exui-page/exui-page';
import { radioButtons } from './create-case-flags-1-content';

export default class CreateCaseFlags1Page extends ExuiPage(BasePage) {
  async verifyContent(
    ccdCaseData: CCDCaseData,
    claimantPartyType: ClaimantDefendantPartyType,
    defendantPartyType: ClaimantDefendantPartyType,
  ) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.caseLevel.label),
      super.expectLabel(
        CaseDataHelper.buildClaimantAndDefendantData(partys.CLAIMANT_1, claimantPartyType)
          .partyName,
      ),
      super.expectLabel(
        CaseDataHelper.buildClaimantAndDefendantData(partys.DEFENDANT_1, defendantPartyType)
          .partyName,
      ),
      super.expectLabel(CaseDataHelper.buildExpertData(partys.CLAIMANT_EXPERT_1).partyName),
      super.expectLabel(CaseDataHelper.buildExpertData(partys.DEFENDANT_1_EXPERT_1).partyName),
      super.expectLabel(CaseDataHelper.buildWitnessData(partys.CLAIMANT_WITNESS_1).partyName),
      super.expectLabel(CaseDataHelper.buildWitnessData(partys.DEFENDANT_1_WITNESS_1).partyName),
    ]);
  }

  async selectCaseLevel() {
    await super.clickBySelector(radioButtons.caseLevel.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
