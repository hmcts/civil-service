import BasePage from '../../../../../base/base-page.ts';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import { heading, radioButtons } from './listing-or-relisting-content.ts';

@AllMethodsStep()
export default class ListingOrRelistingPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectLabel(radioButtons.listing.label),
      super.expectLabel(radioButtons.relisting.label),
    ]);
  }
  async selectListing() {
    await super.clickBySelector(radioButtons.listing.selector);
  }
  async submit() {
    await super.retryClickSubmit();
  }
}
