import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, subheadings, checkboxes } from './document-selection-small-claim-content.ts';
import { Page } from 'playwright-core';
import { Party } from '../../../../../models/users/partys.ts';

@AllMethodsStep()
export default class DocumentSelectionSmallClaimPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectSubheading(subheadings.witnessEvidence),
      super.expectSubheading(subheadings.expertEvidence),
      super.expectSubheading(subheadings.trialDocuments),
      super.expectLabel(checkboxes.witnessStatement.label),
      super.expectLabel(checkboxes.witnessSummary.label),
      super.expectLabel(checkboxes.documentaryEvidence.label),
      super.expectLabel(checkboxes.expertsReport.label),
      super.expectLabel(checkboxes.jointStatementOfExperts.label),
      super.expectLabel(checkboxes.authorities.label),
      super.expectLabel(checkboxes.costs.label),
      super.expectLabel(checkboxes.documentaryEvidenceForTrial.label),
    ]);
  }

  async selectWitnessStatement() {
    await super.clickBySelector(checkboxes.witnessStatement.selector(this.claimantDefendantParty));
  }

  async selectExpertsReport() {
    await super.clickBySelector(checkboxes.expertsReport.selector(this.claimantDefendantParty));
  }

  async selectAuthorities() {
    await super.clickBySelector(checkboxes.authorities.selector(this.claimantDefendantParty));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
