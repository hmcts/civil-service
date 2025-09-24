import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import OrganisationFragment from '../../../../fragments/organisation/organisation-fragment';
import { heading, subheadings } from './defendant-solicitor-organisation-content';

@AllMethodsStep()
export default class DefendantSolicitorOrganisationPage extends ExuiPage(BasePage) {
  private organisationFragment: OrganisationFragment;

  constructor(page: Page, organisationFragment: OrganisationFragment) {
    super(page);
    this.organisationFragment = organisationFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectSubheading(subheadings.defendantLegalRep),
      this.organisationFragment.verifyContent(),
    ]);
  }

  async selectOrganisation() {
    await this.organisationFragment.enterReference();
    await this.organisationFragment.searchForOrganisation('Civil - Organisation 2');
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
