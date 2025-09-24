import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { inputs } from './date-content';
import DateHelper from '../../../../helpers/date-helper';

@AllMethodsStep()
export default class DateFragment extends ExuiPage(BasePage) {
  async verifyContent(
    selectorKey: string,
    {
      index,
      containerSelector,
      count,
    }: { index?: number; containerSelector?: string; count?: number } = {},
  ) {
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
    date: Date,
    selectorKey: string,
    { index, containerSelector }: { index?: number; containerSelector?: string } = {},
  ) {
    await super.inputText(DateHelper.getTwoDigitDay(date), inputs.day.selector(selectorKey), {
      index,
      containerSelector,
    });
    await super.inputText(DateHelper.getTwoDigitMonth(date), inputs.month.selector(selectorKey), {
      index,
      containerSelector,
    });
    await super.inputText(date.getFullYear(), inputs.year.selector(selectorKey), {
      index,
      containerSelector,
    });
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
