import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, inputs } from './create-case-flags-flag-type-case-level-content';
import CaseLevelFlags from '../../../../../../enums/case-flags/case-level-flags';

@AllMethodsStep()
export default class CreateCaseFlagsFlagTypeCaseLevelPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(CaseLevelFlags.COMPLEX),
      super.expectLabel(CaseLevelFlags.URGENT),
      super.expectLabel(CaseLevelFlags.POWER_OF_ARREST),
      super.expectLabel(CaseLevelFlags.WARRANT_OF_ARREST),
      super.expectLabel(CaseLevelFlags.WELSH_FORMS_AND_COMS),
      super.expectLabel(CaseLevelFlags.OTHER),
    ]);
  }

  async selectFlag(caseflag: CaseLevelFlags) {
    await super.clickByLabel(caseflag);
    if (caseflag === CaseLevelFlags.OTHER) {
      await super.inputText(CaseLevelFlags.OTHER, inputs.other.selector);
    }
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
