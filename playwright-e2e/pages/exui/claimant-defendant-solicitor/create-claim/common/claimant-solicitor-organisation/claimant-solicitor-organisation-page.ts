import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import { heading, subheadings } from './claimant-solicitor-organisation-content.ts';
import OrganisationFragment from '../../../../fragments/organisation/organisation-fragment.ts';

@AllMethodsStep()
export default class ClaimantSolicitorOrganisationPage extends ExuiPage(BasePage) {
  private organisationFragment: OrganisationFragment;

  constructor(page: Page, organisationFragment: OrganisationFragment) {
    super(page);
    this.organisationFragment = organisationFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectSubheading(subheadings.claimantLegalRepresentative),
      this.organisationFragment.verifyContent(),
    ]);
  }

  async enterReference() {
    await this.organisationFragment.enterReference();
  }

  async selectOrganisation() {
    await this.organisationFragment.searchForOrganisation('Civil - Organisation 1');
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
