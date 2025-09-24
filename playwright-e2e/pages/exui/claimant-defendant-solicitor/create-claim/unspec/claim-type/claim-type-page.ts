import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { labels, radioButtons } from './claim-type-content';

@AllMethodsStep()
export default class ClaimTypePage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(labels.claimType),
      super.expectLabel(radioButtons.claimType.personalInjury.label),
      super.expectLabel(radioButtons.claimType.clinicalNegligence.label),
      super.expectLabel(radioButtons.claimType.professionalNegligence.label),
      super.expectLabel(radioButtons.claimType.breachOfContract.label),
      super.expectLabel(radioButtons.claimType.consumer.label),
      super.expectLabel(radioButtons.claimType.consumerCredit.label),
      super.expectLabel(radioButtons.claimType.other.label),
    ]);
  }

  async selectPersonalInjury() {
    await super.clickBySelector(radioButtons.claimType.personalInjury.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
