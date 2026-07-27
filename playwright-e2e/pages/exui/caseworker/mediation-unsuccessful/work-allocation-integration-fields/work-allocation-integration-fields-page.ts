import { AllMethodsStep } from '../../../../../decorators/test-steps';
import BasePage from '../../../../../base/base-page';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { workAllocation } from './work-allocation-integration-fields-content';

@AllMethodsStep()
export default class WorkAllocationIntegrationFieldsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectText(workAllocation.allocatedTrack.label),
    ]);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
