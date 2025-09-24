import BasePage from '../../../../base/base-page';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import DateHelper from '../../../../helpers/date-helper';
import ExuiPage from '../../exui-page/exui-page';
import {
  getDefendantHeading,
  subheading,
  table,
} from './certificate-of-service-notify-claim-details-submit-content';

@AllMethodsStep()
export default class CertificateOfServiceNotifyClaimDetailsSubmitFragment extends ExuiPage(
  BasePage,
) {
  async verifyContent() {
    await super.runVerifications(
      [
        super.expectHeading(getDefendantHeading(1)),
        super.expectHeading(getDefendantHeading(2)),
        super.expectSubheading(subheading),
        super.expectText(table.dateDeemedServed.label, { count: 2 }),
        super.expectText(table.dateOfService.label, { count: 2 }),
        super.expectText(table.documentsServed.label, { count: 2 }),
        super.expectText(table.documentsServedLocation.label, { count: 2 }),
        super.expectText(table.name.label, { count: 2 }),
        super.expectText(table.firm.label, { count: 2 }),
        super.expectText(table.locationType.label, { count: 2 }),
        super.expectText(table.notifyClaimRecipient.label, { count: 2 }),
        super.expectText(table.serveType.label, { count: 2 }),
      ],
      { runAxe: false },
    );
  }

  async verifyDefendant1Answers() {
    const dateDeemedServed = DateHelper.getToday();
    const dateOfService = DateHelper.addToToday({
      days: 2,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await super.runVerifications(
      [
        super.expectText(
          DateHelper.formatDateToString(dateDeemedServed, { outputFormat: 'DD Mon YYYY' }),
          { first: true },
        ),
        super.expectText(
          DateHelper.formatDateToString(dateOfService, { outputFormat: 'DD Mon YYYY' }),
          { first: true },
        ),
        super.expectText(table.documentsServed.defendant1Answer),
        super.expectText(table.documentsServedLocation.defendant1Answer),
        super.expectText(table.name.defendant1Answer),
        super.expectText(table.firm.defendant1Answer),
        super.expectText(table.locationType.defendant1Answer),
        super.expectText(table.notifyClaimRecipient.defendant1Answer),
        super.expectText(table.serveType.defendant1Answer),
      ],
      { runAxe: false },
    );
  }

  async verifyDefendant2Answers() {
    const dateDeemedServed = DateHelper.getToday();
    const dateOfService = DateHelper.addToToday({
      days: 2,
      workingDay: true,
      addDayAfter4pm: true,
    });
    await super.runVerifications(
      [
        super.expectText(
          DateHelper.formatDateToString(dateDeemedServed, { outputFormat: 'DD Mon YYYY' }),
          { index: 1 },
        ),
        super.expectText(
          DateHelper.formatDateToString(dateOfService, { outputFormat: 'DD Mon YYYY' }),
          { index: 1 },
        ),
        super.expectText(table.documentsServed.defendant2Answer),
        super.expectText(table.documentsServedLocation.defendant2Answer),
        super.expectText(table.name.defendant2Answer),
        super.expectText(table.firm.defendant2Answer),
        super.expectText(table.locationType.defendant2Answer),
        super.expectText(table.notifyClaimRecipient.defendant2Answer),
        super.expectText(table.serveType.defendant2Answer),
      ],
      { runAxe: false },
    );
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
