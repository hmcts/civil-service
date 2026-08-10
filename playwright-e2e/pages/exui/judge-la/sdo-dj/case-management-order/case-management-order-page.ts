import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import { inputs, radioButtons } from './case-management-order-content';

@AllMethodsStep()
export default class CaseManagementOrderPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData): Promise<void> {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(radioButtons.drawDirectionsOrder.label),
      super.expectRadioLabel(
        radioButtons.drawDirectionsOrder.disposalHearing.label,
        radioButtons.drawDirectionsOrder.disposalHearing.selector,
      ),
      super.expectRadioLabel(
        radioButtons.drawDirectionsOrder.trialHearing.label,
        radioButtons.drawDirectionsOrder.trialHearing.selector,
      ),
    ]);
  }

  async selectDisposalHearing() {
    await super.clickBySelector(radioButtons.drawDirectionsOrder.disposalHearing.selector);
  }

  async selectTrialHearing() {
    await super.clickBySelector(radioButtons.drawDirectionsOrder.trialHearing.selector);

    await Promise.all([
      super.expectLabel(inputs.additionalDirections.buildingDispute.label),
      super.expectLabel(inputs.additionalDirections.clinicalNegligence.label),
      super.expectLabel(inputs.additionalDirections.creditHire.label),
      super.expectLabel(inputs.additionalDirections.employersLiability.label),
      super.expectLabel(inputs.additionalDirections.housingDisrepair.label),
      super.expectLabel(inputs.additionalDirections.personalInjury.label),
      super.expectLabel(inputs.additionalDirections.roadTrafficAccident.label),
    ]);

    await super.clickBySelector(inputs.additionalDirections.buildingDispute.selector);
    await super.clickBySelector(inputs.additionalDirections.clinicalNegligence.selector);
    await super.clickBySelector(inputs.additionalDirections.creditHire.selector);
    await super.clickBySelector(inputs.additionalDirections.employersLiability.selector);
    await super.clickBySelector(inputs.additionalDirections.housingDisrepair.selector);
    await super.clickBySelector(inputs.additionalDirections.personalInjury.selector);
    await super.clickBySelector(inputs.additionalDirections.roadTrafficAccident.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
