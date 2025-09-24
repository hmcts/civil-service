import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { radioButtons } from './yes-or-no-content';

@AllMethodsStep()
export default class YesOrNoFragment extends ExuiPage(BasePage) {
  async verifyContent(
    selectorKey: string,
    {
      index,
      containerSelector,
      count,
    }: { index?: number; containerSelector?: string; count?: number } = {},
  ) {
    await super.expectRadioLabel(radioButtons.yes.label, radioButtons.yes.selector(selectorKey), {
      index,
      containerSelector,
      count,
    });
    await super.expectRadioLabel(radioButtons.no.label, radioButtons.no.selector(selectorKey), {
      index,
      containerSelector,
      count,
    });
  }

  async selectYes(
    selectorKey: string,
    { index, containerSelector }: { index?: number; containerSelector?: string } = {},
  ) {
    await super.clickBySelector(radioButtons.yes.selector(selectorKey), {
      index,
      containerSelector,
    });
  }

  async selectNo(
    selectorKey: string,
    { index, containerSelector }: { index?: number; containerSelector?: string } = {},
  ) {
    await super.clickBySelector(radioButtons.no.selector(selectorKey), {
      index,
      containerSelector,
    });
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
