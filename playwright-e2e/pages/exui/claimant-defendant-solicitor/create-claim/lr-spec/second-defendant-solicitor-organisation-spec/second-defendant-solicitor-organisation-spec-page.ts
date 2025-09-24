import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import OrganisationRegisteredFragment from '../../../../fragments/organisation-registered/organisation-registered-fragment';
import OrganisationFragment from '../../../../fragments/organisation/organisation-fragment';
import { heading, subheadings } from './second-defendant-solicitor-organisation-spec-content';

@AllMethodsStep()
export default class SecondDefendantSolicitorOrganisationSpecPage extends ExuiPage(BasePage) {
  private organisationRegisteredFragment: OrganisationRegisteredFragment;
  private organisationFragment: OrganisationFragment;

  constructor(
    page: Page,
    organisationRegisteredFragment: OrganisationRegisteredFragment,
    organisationFragment: OrganisationFragment,
  ) {
    super(page);
    this.organisationRegisteredFragment = organisationRegisteredFragment;
    this.organisationFragment = organisationFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      this.organisationRegisteredFragment.verifyContent(),
    ]);
  }

  async selectOrganisation() {
    this.organisationRegisteredFragment.selectYes();
    await super.expectSubheading(subheadings.defendantLegalRep),
      await this.organisationFragment.verifyContent();
    await this.organisationFragment.enterReference();
    await this.organisationFragment.searchForOrganisation('Civil - Organisation 3');
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
