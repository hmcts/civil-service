import BasePage from '../../../../../../base/base-page';
import { AllMethodsStep } from '../../../../../../decorators/test-steps';
import CCDCaseData from '../../../../../../models/ccd/ccd-case-data';
import ExuiPage from '../../../../exui-page/exui-page';
import { heading, inputs } from './create-case-flags-flag-type-party-content';
import PartyCaseFlags from '../../../../../../enums/case-flags/party-case-flags';

@AllMethodsStep()
export default class CreateCaseFlagsFlagTypePartyPage extends ExuiPage(BasePage) {
  async verifyContent(ccdCaseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(ccdCaseData),
      super.expectHeading(heading),
      super.expectLabel(PartyCaseFlags.SPECIAL_MEASURE),
      super.expectLabel(PartyCaseFlags.ADJUSTMENT),
      super.expectLabel(PartyCaseFlags.VULNERABLE),
      super.expectLabel(PartyCaseFlags.CONFIDENTIAL),
      super.expectLabel(PartyCaseFlags.BEHAVIOUR),
      super.expectLabel(PartyCaseFlags.VEXATIOUS),
      super.expectLabel(PartyCaseFlags.RESTRAINT),
      super.expectLabel(PartyCaseFlags.BANNING_ORDER),
      super.expectLabel(PartyCaseFlags.EVIDENCE),
      super.expectLabel(PartyCaseFlags.INTERPRETER),
      super.expectLabel(PartyCaseFlags.DEATH),
      super.expectLabel(PartyCaseFlags.LITIGATION_FRIEND),
      super.expectLabel(PartyCaseFlags.LACKING_CAPACITY),
      super.expectLabel(PartyCaseFlags.DETAINED),
      super.expectLabel(PartyCaseFlags.OTHER),
    ]);
  }

  async selectFlag(caseflag: PartyCaseFlags) {
    await super.clickByLabel(caseflag);
    if (caseflag === PartyCaseFlags.OTHER) {
      await super.inputText(PartyCaseFlags.OTHER, inputs.other.selector);
    }
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
