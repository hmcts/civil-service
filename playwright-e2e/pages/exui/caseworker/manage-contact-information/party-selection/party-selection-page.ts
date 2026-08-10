import { Page } from '@playwright/test';
import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { radioButtons } from './party-selection-content';
import { ClaimantDefendantPartyType } from '../../../../../models/users/claimant-defendant-party-types';

@AllMethodsStep()
export default class PartySelectionPage extends ExuiPage(BasePage) {

  constructor(page: Page) {
    super(page);
  }

  async verifyContent(ccdCaseData: CCDCaseData, claimant1PartyType: ClaimantDefendantPartyType, defendant1PartyType: ClaimantDefendantPartyType) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.party.claimant1.label(claimant1PartyType)),
      super.expectLabel(radioButtons.party.claimant1Litigation.label()),
      super.expectLabel(radioButtons.party.claimant1LRIndividuals.label),
      super.expectLabel(radioButtons.party.claimant1Witnesses.label),
      super.expectLabel(radioButtons.party.claimant1Experts.label),
      super.expectLabel(radioButtons.party.defendant1.label(defendant1PartyType)),
      super.expectLabel(radioButtons.party.defendant1LitigationFriend.label()),
      super.expectLabel(radioButtons.party.defendant1LRIndividuals.label),
      super.expectLabel(radioButtons.party.defendant1Witnesses.label),
      super.expectLabel(radioButtons.party.defendant1Experts.label),
    ]);
  }

  async selectClaimant1() {
    await super.clickBySelector(radioButtons.party.claimant1.selector);
  }

  async selectClaimant1LRIndividuals() {
    await super.clickBySelector(radioButtons.party.claimant1LRIndividuals.selector);
  }

  async selectDefendant1() {
    await super.clickBySelector(radioButtons.party.defendant1.selector);
  }

  async selectDefendant1LRIndividuals() {
    await super.clickBySelector(radioButtons.party.defendant1LRIndividuals.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
