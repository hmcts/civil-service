import BasePage from '../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../exui-page/exui-page.ts';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data.ts';
import { Page } from '@playwright/test';
import { ClaimantDefendantPartyType } from '../../../../../../models/claimant-defendant-party-types.ts';
import partys from '../../../../../../constants/partys.ts';
import { inputs, heading } from './confirm-name-and-address-content.ts';
import DateOfBirthFragment from '../../../../fragments/date/date-of-birth-fragment.ts';
import { getFormattedCaseId } from '../../../../exui-page/exui-content.ts';

@AllMethodsStep()
export default class ConfirmNameAndAddress1v2Page extends ExuiPage(BasePage) {
  private dateOfBirthFragment: DateOfBirthFragment;

  constructor(page: Page, dateOfBirthFragment: DateOfBirthFragment) {
    super(page);
    this.dateOfBirthFragment = dateOfBirthFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.expectHeading(heading),
      super.expectHeading(getFormattedCaseId(ccdCaseData.id)),
      super.expectHeading(ccdCaseData.caseNamePublic),
      super.expectText(inputs.dateOfBirth.label, { count: 2 }),
    ]);
  }

  async enterDefendant1DateOfBirth(claimantDefendantPartyType: ClaimantDefendantPartyType) {
    await this.dateOfBirthFragment.enterDate(partys.DEFENDANT_1, claimantDefendantPartyType, {
      index: 0,
    });
  }

  async enterDefendant2DateOfBirth(claimantDefendantPartyType: ClaimantDefendantPartyType) {
    await this.dateOfBirthFragment.enterDate(partys.DEFENDANT_2, claimantDefendantPartyType, {
      index: 1,
    });
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
