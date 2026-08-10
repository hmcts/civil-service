import BasePage from '../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../models/ccd-case-data';
import ExuiPage from '../../../mixin-pages/exui-page/exui-page';
import {
  subheadings,
  inputs,
  radioButtons,
  paragraphs,
  containers,
} from './judge-response-to-reconsideration-content';

@AllMethodsStep()
export default class JudgeResponseToReconsiderationPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectLabel(radioButtons.yes.label),
      super.expectLabel(radioButtons.noCreateNewSdo.label),
      super.expectLabel(radioButtons.noCreateGeneralOrder.label),
    ]);
  }

  async selectYes() {
    await super.clickBySelector(radioButtons.yes.selector);
  }

  async selectNoCreateNewSdo() {
    await super.clickBySelector(radioButtons.noCreateNewSdo.selector);
    await super.expectSubheading(subheadings.createNewSDO, {
      headingLevel: 3,
      containerSelector: containers.createNewSDO,
    });
    await super.expectText(paragraphs.createNewSDO, {
      containerSelector: containers.createNewSDO,
    });
  }

  async selectNoCreateGeneralOrder() {
    await super.clickBySelector(radioButtons.noCreateGeneralOrder.selector);
    await super.expectSubheading(subheadings.createGeneralOrder, {
      headingLevel: 3,
      containerSelector: containers.createGeneralOrder,
    });
    await super.expectText(paragraphs.createGeneralOrder, {
      containerSelector: containers.createGeneralOrder,
    });
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
