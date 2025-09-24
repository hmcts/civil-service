import BasePage from '../../../../../../base/base-page';
import partys from '../../../../../../constants/partys';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import ExuiPage from '../../../../exui-page/exui-page';
import { dropdowns, inputs, paragraphs, subheadings } from './evidence-list-content';

@AllMethodsStep()
export default class EvidenceListPage extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications([
      super.expectSubheading(subheadings.listYourEvidence),
      super.expectText(paragraphs.evidenceInfo),
    ]);
  }

  async addNew() {
    await super.clickAddNew();
  }

  async enterEvidence1Details() {
    await super.selectFromDropdown(dropdowns.evidence.options[0], dropdowns.evidence.selector(1));
    await super.inputText(
      `Contract and Agreements - ${partys.CLAIMANT_1.key}`,
      inputs.evidence.contractAndAgreements.selector(1),
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
