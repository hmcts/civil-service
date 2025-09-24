import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import { subheadings } from './references-content.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import SolicitorReferenceFragment from '../../../../fragments/solicitor-reference/solicitor-reference-fragment.ts';
import { Page } from '@playwright/test';

@AllMethodsStep()
export default class ReferencesPage extends ExuiPage(BasePage) {
  private claimantSolicitorReferenceFragment: SolicitorReferenceFragment;
  private defendantSolicitorReferenceFragment: SolicitorReferenceFragment;

  constructor(
    page: Page,
    claimantSolicitorReferenceFragment: SolicitorReferenceFragment,
    defendantSolicitorReferenceFragment: SolicitorReferenceFragment,
  ) {
    super(page);
    this.claimantSolicitorReferenceFragment = claimantSolicitorReferenceFragment;
    this.defendantSolicitorReferenceFragment = defendantSolicitorReferenceFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectSubheading(subheadings.yourFileReference),
      this.claimantSolicitorReferenceFragment.verifyContent(),
      this.defendantSolicitorReferenceFragment.verifyContent(),
    ]);
  }

  async enterReferences() {
    await this.claimantSolicitorReferenceFragment.enterReference();
    await this.defendantSolicitorReferenceFragment.enterReference();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
