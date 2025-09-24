import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import DateOfBirthFragment from '../../../../../fragments/date/date-of-birth-fragment.ts';
import { Page } from '@playwright/test';
import { ClaimantDefendantPartyType } from '../../../../../../../models/claimant-defendant-party-types.ts';
import partys from '../../../../../../../constants/partys.ts';
import { inputs } from './confirm-details-content.ts';

@AllMethodsStep()
export default class ConfirmDetails1v2Page extends ExuiPage(BasePage) {
  private dateOfBirthFragment: DateOfBirthFragment;

  constructor(page: Page, dateOfBirthFragment: DateOfBirthFragment) {
    super(page);
    this.dateOfBirthFragment = dateOfBirthFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      // this.dateOfBirthFragment.verifyContent(),
      super.expectText(inputs.dateOfBirth.label, { count: 2 }),
    ]);
  }

  async enterDefendant1DateOfBirth(partyType: ClaimantDefendantPartyType) {
    await this.dateOfBirthFragment.enterDate(partys.DEFENDANT_1, partyType, { index: 0 });
  }

  async enterDefendant2DateOfBirth(partyType: ClaimantDefendantPartyType) {
    await this.dateOfBirthFragment.enterDate(partys.DEFENDANT_2, partyType, { index: 1 });
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
