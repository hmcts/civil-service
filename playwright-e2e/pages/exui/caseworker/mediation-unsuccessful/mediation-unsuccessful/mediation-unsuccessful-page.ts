import { AllMethodsStep } from '../../../../../decorators/test-steps';
import BasePage from '../../../../../base/base-page';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import CCDCaseData from '../../../../../models/ccd-case-data';
import { checkboxes } from './mediation-unsuccessful-content';

@AllMethodsStep()
export default class MediationUnsuccessfulPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(checkboxes.partyWithdraws.label),
      super.expectLabel(checkboxes.appointmentNoAgreement.label),
      super.expectLabel(checkboxes.appointmentNotAssigned.label),
      super.expectLabel(checkboxes.notContactableClaimantOne.label),
      super.expectLabel(checkboxes.notContactableClaimantTwo.label),
      super.expectLabel(checkboxes.notContactableDefendantOne.label),
      super.expectLabel(checkboxes.notContactableDefendantTwo.label)
    ]);
  }

  async selectAppointmentNoAgreement() {
    await super.clickByLabel(checkboxes.appointmentNoAgreement.label);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
