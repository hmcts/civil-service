import BasePageFactory from '../../../../base/base-page-factory';
import SubmitDefaultJudgmentPage from './common/submit-default-judgment/submit-default-judgment';
import ClaimPartialPayment1v2Page from './lr-spec/claim-partial-payment/claim-partial-payment-1v2-page';
import ClaimPartialPaymentPage from './lr-spec/claim-partial-payment/claim-partial-payment-page';
import DefendantDetailsSpec1v2Page from './lr-spec/defendant-details-spec/defendant-details-spec-1v2-page';
import DefendantDetailsSpecPage from './lr-spec/defendant-details-spec/defendant-details-spec-page';
import FixedCostsOnEntryPage from './lr-spec/fixed-costs-on-entry/fixed-costs-on-entry-page';
import PaymentBreakdown1v2Page from './lr-spec/payment-breakdown/payment-breakdown-1v2-page';
import PaymentBreakdownPage from './lr-spec/payment-breakdown/payment-breakdown-page';
import PaymentSetDate1v2Page from './lr-spec/payment-set-date/payment-set-date-1v2-page';
import PaymentSetDatePage from './lr-spec/payment-set-date/payment-set-date-page';
import PaymentType1v2Page from './lr-spec/payment-type/payment-type-1v2-page';
import PaymentTypePage from './lr-spec/payment-type/payment-type-page';
import RepaymentInformation1v2Page from './lr-spec/repayment-information/repayment-information-1v2-page';
import RepaymentInformationPage from './lr-spec/repayment-information/repayment-information-page';
import ShowCertifyStatmentSpecMultipleDefendantPage from './lr-spec/show-certify-statement-spec-multiple-defendant/show-certify-statement-spec-multiple-defendant-page';
import ShowCertifyStatmentSpecPage from './lr-spec/show-certify-statement-spec/show-certify-statement-spec-page';
import DefendantDetails1v2Page from './unspec/defendant-details/defendant-details-1v2-page';
import DefendantDetailsPage from './unspec/defendant-details/defendant-details-page';
import HearingSupportRequirementsFieldDJPage from './unspec/hearing-support-requirements-field-dj/hearing-support-requirements-field-dj-page';
import HearingTypePage from './unspec/hearing-type/hearing-type-page';
import ShowCertifyStatmentBothPage from './unspec/show-certify-statement-both/show-certify-statement-both-page';
import ShowCertifyStatmentPage from './unspec/show-certify-statement/show-certify-statement-page';
import DateFragment from '../../fragments/date/date-fragment.ts';
import YesOrNoFragment from '../../fragments/yes-or-no/yes-or-no-fragment.ts';
import ConfirmDefaultJudgmentPage from './unspec/confirm-default-judgment/confirm-default-judgment-page.ts';
import ConfirmDefaultJudgmentSpecPage from './lr-spec/confirm-default-judgment-spec/confirm-default-judgment-spec-page.ts';

export default class DefaultJudgmentPageFactory extends BasePageFactory {
  get defendantDetailsPage() {
    return new DefendantDetailsPage(this.page);
  }

  get defendantDetails1v2Page() {
    return new DefendantDetails1v2Page(this.page);
  }

  get defendantDetailsSpecPage() {
    return new DefendantDetailsSpecPage(this.page);
  }

  get defendantDetailsSpec1v2Page() {
    return new DefendantDetailsSpec1v2Page(this.page);
  }

  get showCertifyStatementPage() {
    return new ShowCertifyStatmentPage(this.page);
  }

  get showCertifyStatementBothPage() {
    return new ShowCertifyStatmentBothPage(this.page);
  }

  get showCertifyStatementSpecPage() {
    return new ShowCertifyStatmentSpecPage(this.page);
  }

  get showCertifyStatementSpecMultipleDefendantPage() {
    return new ShowCertifyStatmentSpecMultipleDefendantPage(this.page);
  }

  get hearingTypePage() {
    return new HearingTypePage(this.page);
  }

  get hearingSupportRequirementsFieldDJPage() {
    return new HearingSupportRequirementsFieldDJPage(this.page);
  }

  get claimPartialPaymentPage() {
    return new ClaimPartialPaymentPage(this.page);
  }

  get claimPartialPayment1v2Page() {
    return new ClaimPartialPayment1v2Page(this.page);
  }

  get fixedCostsOnEntryPage() {
    const yesOrNoFragment = new YesOrNoFragment(this.page);
    return new FixedCostsOnEntryPage(this.page, yesOrNoFragment);
  }

  get paymentBreakdownPage() {
    return new PaymentBreakdownPage(this.page);
  }

  get paymentBreakdown1v2Page() {
    return new PaymentBreakdown1v2Page(this.page);
  }

  get paymentTypePage() {
    return new PaymentTypePage(this.page);
  }

  get paymentType1v2Page() {
    return new PaymentType1v2Page(this.page);
  }

  get paymentSetDatePage() {
    const dateFragment = new DateFragment(this.page);
    return new PaymentSetDatePage(this.page, dateFragment);
  }

  get paymentSetDate1v2Page() {
    return new PaymentSetDate1v2Page(this.page);
  }

  get repaymentInformationPage() {
    const dateFragment = new DateFragment(this.page);
    return new RepaymentInformationPage(this.page, dateFragment);
  }

  get repaymentInformation1v2Page() {
    const dateFragment = new DateFragment(this.page);
    return new RepaymentInformation1v2Page(this.page, dateFragment);
  }

  get submitDefaultJudgmentPage() {
    return new SubmitDefaultJudgmentPage(this.page);
  }

  get confirmDefaultJudgmentPage() {
    return new ConfirmDefaultJudgmentPage(this.page);
  }

  get confirmDefaultJudgmentSpecPage() {
    return new ConfirmDefaultJudgmentSpecPage(this.page);
  }
}
