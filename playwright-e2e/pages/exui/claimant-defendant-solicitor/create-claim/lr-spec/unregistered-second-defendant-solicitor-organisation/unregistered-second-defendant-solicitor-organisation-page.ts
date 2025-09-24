import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import UnregisteredOrganisationFragment from '../../../../fragments/unregistered-organisation/unregistered-organisation-fragment.ts';
import { heading } from './unregistered-second-defendant-solicitor-organisation-content.ts';
import UnregisteredOrganisationAddressFragment from '../../../../fragments/unregistered-organisation-address/unregistered-organisation-address-fragment.ts';

@AllMethodsStep()
export default class UnregisteredSecondDefendantSolicitorOrganisationPage extends ExuiPage(
  BasePage,
) {
  private unregisteredOrganisationFragment: UnregisteredOrganisationFragment;
  private unregisteredOrganisationAddressFragment: UnregisteredOrganisationAddressFragment;

  constructor(
    page: Page,
    unregisteredOrganisationFragment: UnregisteredOrganisationFragment,
    unregisteredOrganisationAddressFragment: UnregisteredOrganisationAddressFragment,
  ) {
    super(page);
    this.unregisteredOrganisationFragment = unregisteredOrganisationFragment;
    this.unregisteredOrganisationAddressFragment = unregisteredOrganisationAddressFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      this.unregisteredOrganisationFragment.verifyContent(),
    ]);
  }

  async enterDetails() {
    await this.unregisteredOrganisationFragment.enterUnregisteredOrgDetails();
    await this.unregisteredOrganisationAddressFragment.enterAddressManual();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
