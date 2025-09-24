import { Page } from '@playwright/test';
import BasePage from '../../../../base/base-page';
import { Party } from '../../../../models/partys';
import ExuiPage from '../../exui-page/exui-page';
import { inputs } from './statement-of-truth-content';
import StringHelper from '../../../../helpers/string-helper';
import { AllMethodsStep } from '../../../../decorators/test-steps';

@AllMethodsStep()
export default class StatementOfTruthFragment extends ExuiPage(BasePage) {
  private solicitorParty: Party;

  constructor(page: Page, solicitorParty: Party) {
    super(page);
    this.solicitorParty = solicitorParty;
  }

  async verifyContent() {
    await super.runVerifications(
      [
        //super.expectSubheading(subheadings.statementOfTruth), TODO - This Sub heading Does no Exist on the Page.
        super.expectLabel(inputs.name.label),
        super.expectLabel(inputs.role.label),
      ],
      { runAxe: false },
    );
  }

  async enterDetails() {
    await super.inputText(StringHelper.capitalise(this.solicitorParty.key), inputs.name.selector);
    await super.inputText(
      StringHelper.capitalise(this.solicitorParty.partyType),
      inputs.role.selector,
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
