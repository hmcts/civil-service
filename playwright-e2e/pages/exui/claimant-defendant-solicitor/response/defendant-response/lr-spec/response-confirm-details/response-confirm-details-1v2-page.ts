import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import {
  heading,
  radioButtons,
  inputs,
  tableHeadings,
} from './response-confirm-details-content.ts';
import { Page } from '@playwright/test';
import SolicitorReferenceFragment from '../../../../../fragments/solicitor-reference/solicitor-reference-fragment.ts';
import partys from '../../../../../../../constants/partys.ts';
import DateOfBirthFragment from '../../../../../fragments/date/date-of-birth-fragment.ts';

@AllMethodsStep()
export default class ResponseConfirmDetails1v2Page extends ExuiPage(BasePage) {
  private solicitorReferenceFragment: SolicitorReferenceFragment;
  private dateOfBirthFragment: DateOfBirthFragment;

  constructor(
    page: Page,
    solicitorReferenceFragment: SolicitorReferenceFragment,
    dateOfBirthFragment: DateOfBirthFragment,
  ) {
    super(page);
    this.solicitorReferenceFragment = solicitorReferenceFragment;
    this.dateOfBirthFragment = dateOfBirthFragment;
  }

  async verifyContent() {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectText(tableHeadings.organisation),
      super.expectText(tableHeadings.reference),
      super.expectText(inputs.defendant1DateOfBirth.label),
      super.expectText(inputs.defendant2DateOfBirth.label),
      this.solicitorReferenceFragment.verifyContent(),
      // this.dateOfBirthFragment.verifyContent(),
      // super.expectText(radioButtons.address.label),
    ]);
  }

  async selectYesAddressBothDefendants() {
    await super.clickBySelector(radioButtons.address.yes.selector(partys.DEFENDANT_1));
    await super.clickBySelector(radioButtons.address.yes.selector(partys.DEFENDANT_2));
  }

  async selectNoAddressBothDefendants() {
    await super.clickBySelector(radioButtons.address.yes.selector(partys.DEFENDANT_1));
    await super.clickBySelector(radioButtons.address.yes.selector(partys.DEFENDANT_2));
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
