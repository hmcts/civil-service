import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, subheadings, checkboxes } from './document-selection-fast-track-content.ts';
import { Page } from 'playwright-core';
import { Party } from '../../../../../models/users/partys.ts';

@AllMethodsStep()
export default class DocumentSelectionFastTrackPage extends ExuiPage(BasePage) {
  private claimantDefendantParty: Party;

  constructor(page: Page, claimantDefendantParty: Party) {
    super(page);
    this.claimantDefendantParty = claimantDefendantParty;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectSubheading(subheadings.disclosure),
      super.expectSubheading(subheadings.witnessEvidence),
      super.expectSubheading(subheadings.expertEvidence),
      super.expectSubheading(subheadings.trialDocuments),
      super.expectLabel(checkboxes.disclosureList.label),
      super.expectLabel(checkboxes.documentsForDisclosure.label),
      super.expectLabel(checkboxes.witnessStatement.label),
      super.expectLabel(checkboxes.witnessSummary.label),
      super.expectLabel(checkboxes.noticeOfIntention.label),
      super.expectLabel(checkboxes.documentsReferred.label),
      super.expectLabel(checkboxes.expertsReport.label),
      super.expectLabel(checkboxes.jointStatement.label),
      super.expectLabel(checkboxes.questionsForExperts.label),
      super.expectLabel(checkboxes.answersForExperts.label),
      super.expectLabel(checkboxes.caseSummary.label),
      super.expectLabel(checkboxes.skeletonArgument.label),
      super.expectLabel(checkboxes.authorities.label),
      super.expectLabel(checkboxes.costs.label),
      super.expectLabel(checkboxes.documentary.label),
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
