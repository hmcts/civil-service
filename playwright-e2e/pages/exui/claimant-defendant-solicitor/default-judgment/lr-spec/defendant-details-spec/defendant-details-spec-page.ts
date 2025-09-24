import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types';
import partys from '../../../../../../constants/partys';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';
import { radioButtons } from './defendant-details-spec-content';
import CCDCaseData from "../../../../../../models/ccd/ccd-case-data.ts";

@AllMethodsStep()
export default class DefendantDetailsSpecPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData:CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.selectDefendant.label),
    ]);
  }

  async selectDefendant(defendantPartyType: ClaimantDefendantPartyType) {
    const defendant1Data = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_1,
      defendantPartyType,
    );
    await super.clickByLabel(
      radioButtons.selectDefendant.defendant.label(defendant1Data.partyName),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
