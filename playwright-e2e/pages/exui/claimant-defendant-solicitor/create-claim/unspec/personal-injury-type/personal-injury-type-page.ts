import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { radioButtons } from './personal-injury-type-content';

@AllMethodsStep()
export default class PersonalInjuryTypePage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.verifyHeadings(),
      super.expectLegend(radioButtons.personalInjuryType.label),
      super.expectLabel(radioButtons.personalInjuryType.roadAccident.label),
      super.expectLabel(radioButtons.personalInjuryType.publicLiability.label),
      super.expectLabel(radioButtons.personalInjuryType.workAccident.label),
      super.expectLabel(radioButtons.personalInjuryType.publicLiability.label),
      super.expectLabel(radioButtons.personalInjuryType.holidayIllness.label),
      super.expectLabel(radioButtons.personalInjuryType.diseaseClaim.label),
      super.expectLabel(radioButtons.personalInjuryType.noiseInducedHearingLoss.label),
      super.expectLabel(radioButtons.personalInjuryType.personalInjuryOther.label),
    ]);
  }

  async selectRoadAccident() {
    await super.clickBySelector(radioButtons.personalInjuryType.roadAccident.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
