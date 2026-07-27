import BasePage from '../../../../base/base-page';
import CCDCaseData from '../../../../models/ccd-case-data';
import { buttons } from './exui-hearings-content';

export default function ExuiHearingsPage<TBase extends abstract new (...args: any[]) => BasePage>(
  Base: TBase,
) {
  abstract class ExuiHearingsPage extends Base {
    protected async clickContinue() {
      await super.clickButtonByName(buttons.continue.title);
    }

    protected async submitRequest() {
      await super.clickButtonByName(buttons.submitRequest.title);
    }

    protected async submitUpdatedRequest() {
      await super.clickButtonByName(buttons.submitUpdatedRequest.title);
    }

    protected async verifyCaseName(
      ccdCaseData: CCDCaseData,
      options: { ignoreDuplicates?: boolean } = {},
    ) {
      await super.expectText(`${ccdCaseData.caseNamePublic}`, {
        exact: false,
        ignoreDuplicates: options.ignoreDuplicates,
      });
    }

    abstract continue(...args: any[]): Promise<void>;
  }

  return ExuiHearingsPage;
}
