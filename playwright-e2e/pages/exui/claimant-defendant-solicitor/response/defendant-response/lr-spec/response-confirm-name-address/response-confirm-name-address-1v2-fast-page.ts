import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import ExuiPage from '../../../../../exui-page/exui-page.ts';
import { radioButtons } from './response-confirm-name-address-content.ts';
import CCDCaseData from '../../../../../../../models/ccd/ccd-case-data.ts';
import partys from '../../../../../../../constants/partys.ts';
import ClaimTrack from '../../../../../../../enums/claim-track.ts';

@AllMethodsStep()
export default class ResponseConfirmNameAddress1v2FastPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.address.label, { count: 2 }),
      super.expectRadioYesLabel(radioButtons.address.yes.selector(partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1)),
      super.expectRadioNoLabel(radioButtons.address.no.selector(partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1)),
    ]);
  }

  async selectYesAddress() {
    await super.clickBySelector(
      radioButtons.address.yes.selector(partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1),
    );
    await super.clickBySelector(
      radioButtons.address.yes.selector(
        partys.DEFENDANT_2,
        partys.DEFENDANT_SOLICITOR_1,
        ClaimTrack.FAST_CLAIM,
      ),
    );
  }

  async selectNoAddress() {
    await super.clickBySelector(
      radioButtons.address.no.selector(partys.DEFENDANT_1, partys.DEFENDANT_SOLICITOR_1),
    );
    await super.clickBySelector(
      radioButtons.address.no.selector(
        partys.DEFENDANT_2,
        partys.DEFENDANT_SOLICITOR_1,
        ClaimTrack.FAST_CLAIM,
      ),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
