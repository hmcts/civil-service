import config from '../../../../config/config';
import BasePage from '../../../../base/base-page';
import urls from '../../../../config/urls';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { dropdowns } from './case-filter-content';
import { buttons } from '../../exui-page/exui-content';
import { CCDEvent } from '../../../../models/ccd/ccd-events';

@AllMethodsStep()
export default class CaseFilterPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectLabel(dropdowns.jurisdiction.label),
      super.expectLabel(dropdowns.caseType.label),
      super.expectLabel(dropdowns.event.label),
    ]);
  }

  async chooseClaimType(ccdEvent: CCDEvent) {
    await super.selectFromDropdown(
      dropdowns.jurisdiction.options.civil,
      dropdowns.jurisdiction.selector,
    );
    await super.selectFromDropdown(dropdowns.caseType.options.civil, dropdowns.caseType.selector);
    await super.selectFromDropdown(ccdEvent.name, dropdowns.event.selector);
  }

  async chooseClaimTypeWithUrl(ccdEvent: CCDEvent) {
    console.log(`Starting event with url: ${ccdEvent.id}`);
    await super.retryGoTo(
      `${urls.manageCase}/cases/case-create/${config.definition.jurisdiction}/${config.definition.caseType}/${ccdEvent.id}/${ccdEvent.id}`,
      async () =>
        super.expectHeading(ccdEvent.name, {
          exact: false,
          timeout: 30_000,
        }),
      undefined,
      { retries: 2, message: `Starting event with url: ${ccdEvent.id} failed, trying again` },
    );
    super.setCCDEvent = ccdEvent;
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
