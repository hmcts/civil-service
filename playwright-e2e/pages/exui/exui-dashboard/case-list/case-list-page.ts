import urls from '../../../../config/urls';
import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';

@AllMethodsStep()
export default class CaseListPage extends BasePage {
  async verifyContent(): Promise<void> {
    throw new Error('Method not implemented.');
  }

  async openCaseList() {
    await super.goTo(`${urls.manageCase}/cases`);
  }
}
