import { Page } from '@playwright/test';
import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import {
  heading,
  paragraphs,
  radioButtons,
} from './spec-respondent-correspondence-address-content';
import CorrespondenceAddressFragment from '../../../../fragments/correspondence-address/correspondence-address-fragment';

@AllMethodsStep()
export default class SpecRespondentCorrespondenceAddressPage extends ExuiPage(BasePage) {
  private correspondenceAddressFragment: CorrespondenceAddressFragment;

  constructor(page: Page, correspondenceAddressFragment: CorrespondenceAddressFragment) {
    super(page);
    this.correspondenceAddressFragment = correspondenceAddressFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectText(paragraphs.descriptionText),
      super.expectLegend(radioButtons.addressRequired.label),
      this.correspondenceAddressFragment.verifyContent(),
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
