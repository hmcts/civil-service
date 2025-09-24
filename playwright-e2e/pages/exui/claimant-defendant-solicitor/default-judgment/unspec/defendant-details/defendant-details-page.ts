import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types';
import partys from '../../../../../../constants/partys';
import { radioButtons } from './defendant-details-content';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';

@AllMethodsStep()
export default class DefendantDetailsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.selectDefendant.label),
    ]);
  }

  async selectDefendant(defendant1PartyType: ClaimantDefendantPartyType) {
    const defendant1Data = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_1,
      defendant1PartyType,
    );
    await super.clickByLabel(
      radioButtons.selectDefendant.defendant.label(defendant1Data.partyName),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
