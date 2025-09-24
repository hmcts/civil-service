import BasePage from '../../../../base/base-page';
import { CCDEvent } from '../../../../models/ccd/ccd-events';
import ExuiPage from '../../exui-page/exui-page';
import { inputs } from './event-summary-content';

export default class EventSummaryFragment extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications(
      [super.expectLabel(inputs.summary.label), super.expectLabel(inputs.description.label)],
      {
        runAxe: false,
      },
    );
  }

  async enterEventDetails(ccdEvent: CCDEvent) {
    super.inputText(ccdEvent.name, inputs.summary.selector);
    super.inputText(ccdEvent.name, inputs.description.selector);
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
