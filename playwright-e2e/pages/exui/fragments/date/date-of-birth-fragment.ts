import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../mixin-pages/exui-page/exui-page';
import { inputs } from './date-content';
import DateHelper from '../../../../helpers/date-helper';
import { Party } from '../../../../models/users/partys';
import { ClaimantDefendantPartyType } from '../../../../models/users/claimant-defendant-party-types';
import CaseDataHelper from '../../../../helpers/case-data-helper';

@AllMethodsStep()
export default class DateOfBirthFragment extends ExuiPage(BasePage) {
  async verifyContent(
    claimantDefendantPartyType: ClaimantDefendantPartyType,
    {
      index,
      containerSelector,
      count,
    }: { index?: number; containerSelector?: string; count?: number } = {},
  ) {
    const selectorKey = `${claimantDefendantPartyType.key}DateOfBirth`;
    await super.expectRadioLabel(inputs.day.label, inputs.day.selector(selectorKey), {
      index,
      containerSelector,
      count,
    });
    await super.expectRadioLabel(inputs.month.label, inputs.month.selector(selectorKey), {
      index,
      containerSelector,
      count,
    });
    await super.expectRadioLabel(inputs.year.label, inputs.year.selector(selectorKey), {
      index,
      containerSelector,
      count,
    });
  }

  async enterDate(
    claimantDefendantParty: Party,
    claimantDefendantPartyType: ClaimantDefendantPartyType,
    { index, containerSelector, updated }: { index?: number; containerSelector?: string; updated?: boolean } = {},
  ) {
    const selectorKey = `${claimantDefendantPartyType.key}DateOfBirth`;
    // const dateOfBirth = new Date(CaseDataHelper.getPartyDateOfBirth(claimantDefendantParty));
    const dobString = updated
    ? CaseDataHelper.getPartyDateOfBirthUpdated(claimantDefendantParty)
    : CaseDataHelper.getPartyDateOfBirth(claimantDefendantParty);

    const dateOfBirth = new Date(dobString);

    await super.inputText(
      DateHelper.getTwoDigitDay(dateOfBirth),
      inputs.day.selector(selectorKey),
      { index, containerSelector },
    );
    await super.inputText(
      DateHelper.getTwoDigitMonth(dateOfBirth),
      inputs.month.selector(selectorKey),
      { index },
    );
    await super.inputText(dateOfBirth.getFullYear(), inputs.year.selector(selectorKey), {
      index,
      containerSelector,
    });
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
