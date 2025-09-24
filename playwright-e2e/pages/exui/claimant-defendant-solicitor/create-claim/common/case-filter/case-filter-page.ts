import BasePage from '../../../../../../base/base-page';
import ccdEvents from '../../../../../../constants/ccd-events';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import { buttons } from '../../../../exui-page/exui-content';
import ExuiPage from '../../../../exui-page/exui-page';
import { dropdowns } from './case-filter-content';

@AllMethodsStep()
export default class CaseFilterPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectLabel(dropdowns.jurisdiction.label),
      super.expectLabel(dropdowns.caseType.label),
      super.expectLabel(dropdowns.event.label),
    ]);
  }

  async chooseSpec() {
    await super.selectFromDropdown(
      dropdowns.jurisdiction.options.civil,
      dropdowns.jurisdiction.selector,
    );
    await super.selectFromDropdown(dropdowns.caseType.options.civil, dropdowns.caseType.selector);
    await super.selectFromDropdown(dropdowns.event.options.spec, dropdowns.event.selector);
  }

  async chooseUnSpec() {
    await super.selectFromDropdown(
      dropdowns.jurisdiction.options.civil,
      dropdowns.jurisdiction.selector,
    );
    await super.selectFromDropdown(dropdowns.caseType.options.civil, dropdowns.caseType.selector);
    await super.selectFromDropdown(dropdowns.event.options.unspec, dropdowns.event.selector);
  }

  async submit() {
    await super.retryClickBySelector(
      buttons.submit.selector,
      () =>
        super.expectNoLabel(dropdowns.jurisdiction.label, {
          timeout: 5_000,
          exact: true,
        }),
      undefined,
      { retries: 3 },
    );
  }
}
