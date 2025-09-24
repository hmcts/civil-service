import BasePage from '../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../models/ccd/ccd-case-data.ts';
import ExuiPage from '../../../exui-page/exui-page.ts';
import EventSummaryFragment from '../../../fragments/event-summary/event-summary-fragment.ts';
import { Page } from '@playwright/test';
import ccdEvents from '../../../../../constants/ccd-events.ts';

@AllMethodsStep()
export default class ManageDocumentSubmitPage extends ExuiPage(BasePage) {
  private eventSummaryFragment: EventSummaryFragment;

  constructor(page: Page, eventSummaryFragment: EventSummaryFragment) {
    super(page);
    this.eventSummaryFragment = eventSummaryFragment;
  }

  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      this.eventSummaryFragment.verifyContent(),
    ]);
  }

  async enterEventDetails() {
    this.eventSummaryFragment.enterEventDetails(ccdEvents.MANAGE_DOCUMENTS);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
