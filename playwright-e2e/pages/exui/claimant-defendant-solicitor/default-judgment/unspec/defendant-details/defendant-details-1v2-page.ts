import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons } from './defendant-details-content';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import partys from '../../../../../../constants/partys';
import CaseDataHelper from '../../../../../../helpers/case-data-helper';

@AllMethodsStep()
export default class DefendantDetails1v2Page extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.selectDefendant.label),
      super.expectLabel(radioButtons.selectDefendant.both.label),
    ]);
  }

  async selectDefendant1(defendantPartyType: ClaimantDefendantPartyType) {
    const defendant1Data = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_2,
      defendantPartyType,
    );
    await super.clickByLabel(
      radioButtons.selectDefendant.defendant.label(defendant1Data.partyName),
    );
  }

  async selectDefendant2(defendantPartyType: ClaimantDefendantPartyType) {
    const defendant2Data = CaseDataHelper.buildClaimantAndDefendantData(
      partys.DEFENDANT_2,
      defendantPartyType,
    );
    await super.clickByLabel(
      radioButtons.selectDefendant.defendant.label(defendant2Data.partyName),
    );
  }

  async selectBothDefendants() {
    await super.clickByLabel(radioButtons.selectDefendant.both.label);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
