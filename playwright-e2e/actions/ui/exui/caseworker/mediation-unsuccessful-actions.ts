import BaseTestData from '../../../../base/base-test-data.ts';
import { AllMethodsStep } from '../../../../decorators/test-steps.ts';
import TestData from '../../../../models/test-utils/test-data.ts';
import MediationUnsuccessfulPageFactory from '../../../../pages/exui/caseworker/mediation-unsuccessful/mediation-unsuccessful-page-factory.ts';

@AllMethodsStep()
export default class MediationUnsuccessfulActions extends BaseTestData {
  private mediationUnsuccessfulPageFactory: MediationUnsuccessfulPageFactory;

  constructor(
    mediationUnsuccessfulPageFactory: MediationUnsuccessfulPageFactory,
    testData: TestData,
  ) {
    super(testData);
    this.mediationUnsuccessfulPageFactory = mediationUnsuccessfulPageFactory;
  }

  async mediationUnsuccessful() {
    const { mediationUnsuccessfulPage } = this.mediationUnsuccessfulPageFactory;
    await mediationUnsuccessfulPage.verifyContent(this.ccdCaseData);
    await mediationUnsuccessfulPage.selectAppointmentNoAgreement();
    await mediationUnsuccessfulPage.submit();

    const { workAllocationIntegrationFieldsPage } = this.mediationUnsuccessfulPageFactory;
    await workAllocationIntegrationFieldsPage.verifyContent(this.ccdCaseData);
    await workAllocationIntegrationFieldsPage.submit();

    const { mediationUnsuccessfulSubmitPage } = this.mediationUnsuccessfulPageFactory;
    await mediationUnsuccessfulSubmitPage.verifyContent(this.ccdCaseData);
    await mediationUnsuccessfulSubmitPage.submit();
  }
}
