import { Page } from 'playwright-core';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading } from './respondent-2-correspondence-address-content';
import CorrespondenceAddressFragment from '../../../../fragments/correspondence-address/correspondence-address-fragment';

@AllMethodsStep()
export default class SpecRespondent2CorrespondenceAddressPage extends ExuiPage(BasePage) {
  private correspondenceAddressFragment: CorrespondenceAddressFragment;

  constructor(page: Page, correspondenceAddressFragment: CorrespondenceAddressFragment) {
    super(page);
    this.correspondenceAddressFragment = correspondenceAddressFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      // this.correspondenceAddressFragment.verifyContent(),
    ]);
  }

  async selectYesAndEnterAddress() {
    await this.correspondenceAddressFragment.selectYes();
    await this.correspondenceAddressFragment.enterAddressManual();
  }

  async selectNo() {
    await this.correspondenceAddressFragment.selectNo();
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
