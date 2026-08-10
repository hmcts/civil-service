import BasePageFactory from '../../../../base/base-page-factory.ts';
import MediationUnsuccessfulPage from './mediation-unsuccessful/mediation-unsuccessful-page.ts';
import WorkAllocationIntegrationFieldsPage from './work-allocation-integration-fields/work-allocation-integration-fields-page.ts';
import MediationUnsuccessfulSubmitPage from './mediation-unsuccessful-submit/mediation-unsuccessful-submit-page.ts';

export default class MediationUnsuccessfulPageFactory extends BasePageFactory {
  get mediationUnsuccessfulPage() {
    return new MediationUnsuccessfulPage(this.page);
  }

  get workAllocationIntegrationFieldsPage() {
    return new WorkAllocationIntegrationFieldsPage(this.page);
  }

  get mediationUnsuccessfulSubmitPage() {
    return new MediationUnsuccessfulSubmitPage(this.page);
  }
}
