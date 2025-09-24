import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, radioButtons } from './claimant-solicitor-service-address-content';
import ServiceAddressFragment from '../../../../fragments/service-address/service-address-fragment';

@AllMethodsStep()
export default class ClaimantSolicitorServiceAddressPage extends ExuiPage(BasePage) {
  serviceAddressFragment: ServiceAddressFragment;

  constructor(page: Page, serviceAddressFragment: ServiceAddressFragment) {
    super(page);
    this.serviceAddressFragment = serviceAddressFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading.legalRepresentativeServiceAddress),
      super.expectLegend(radioButtons.addressRequired.label),
      super.expectText(radioButtons.addressRequired.hintText),
      this.serviceAddressFragment.verifyContent(),
    ]);
  }

  async selectYesAndEnterAddress() {
    await this.serviceAddressFragment.selectYes();
    await this.serviceAddressFragment.enterAddressManual();
  }

  async selectNo() {
    await this.serviceAddressFragment.selectNo();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
