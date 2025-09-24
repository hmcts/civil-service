import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../exui-page/exui-page';
import { checkboxes, radioButtons } from './order-type-content';

@AllMethodsStep()
export default class OrderTypePage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.orderType.label),
      super.expectLabel(radioButtons.orderType.disposal.label),
      super.expectLabel(radioButtons.orderType.trail.label),
    ]);
  }

  async selectDisposalHearing() {
    await super.clickBySelector(radioButtons.orderType.disposal.selector);
  }

  async selectTrail() {
    await super.clickBySelector(radioButtons.orderType.trail.selector);
    Promise.all([
      super.expectLabel(checkboxes.buildingDispute.label, { count: 1 }),
      super.expectLabel(checkboxes.clinicialNegligence.label, { count: 1 }),
      super.expectLabel(checkboxes.creditHire.label, { count: 1 }),
      super.expectLabel(checkboxes.employersLiability.label, { count: 1 }),
      super.expectLabel(checkboxes.housingDisrepair.label, { count: 1 }),
      super.expectLabel(checkboxes.personalInjury.label, { count: 1 }),
      super.expectLabel(checkboxes.roadTrafficAccident.label, { count: 1 }),
      super.expectLabel(checkboxes.noiseInducedHearingLoss.label, { count: 1 }),
    ]);
  }

  async setAdditionalDirections() {
    await super.clickBySelector(checkboxes.buildingDispute.selector);
    await super.clickBySelector(checkboxes.clinicialNegligence.selector);
    await super.clickBySelector(checkboxes.creditHire.selector);
    await super.clickBySelector(checkboxes.employersLiability.selector);
    await super.clickBySelector(checkboxes.housingDisrepair.selector);
    await super.clickBySelector(checkboxes.personalInjury.selector);
    await super.clickBySelector(checkboxes.roadTrafficAccident.selector);
  }

  async setNoiseInducedHearingLoss() {
    await super.clickBySelector(checkboxes.noiseInducedHearingLoss.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
