import BasePage from '../../../../../../../base/base-page.ts';
import { AllMethodsStep } from '../../../../../../../decorators/test-steps.ts';
import CCDCaseData from '../../../../../../../models/ccd-case-data.ts';
import ExuiPage from '../../../../../mixin-pages/exui-page/exui-page.ts';
import { radioButtons, inputs } from './defendant-partners-and-dependents-content.ts';

@AllMethodsStep()
export default class DefendantPartnersAndDependentsPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLegend(radioButtons.liveWithPartner.label),
      super.expectLegend(radioButtons.haveAnyChildren.label),
      super.expectLegend(radioButtons.supportedAnyoneFinancial.label),
    ]);
  }

  async enterDetails() {
    await super.clickBySelector(radioButtons.liveWithPartner.yes.selector);
    await super.clickBySelector(radioButtons.partnerAgedOver.yes.selector);
    await super.clickBySelector(radioButtons.haveAnyChildren.yes.selector);
    await super.inputText('1', inputs.numberOfUnderEleven.selector);
    await super.inputText('1', inputs.numberOfElevenToFifteen.selector);
    await super.inputText('0', inputs.numberOfSixteenToNineteen.selector);
    await super.clickBySelector(radioButtons.receiveDisabilityPayments.yes.selector);
    await super.clickBySelector(radioButtons.supportedAnyoneFinancial.yes.selector);
    await super.inputText('2', inputs.supportPeopleNumber.selector);
    await super.inputText('details', inputs.supportPeopleDetails.selector);
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
