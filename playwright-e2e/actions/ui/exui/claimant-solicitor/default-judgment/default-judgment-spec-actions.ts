import TestData from '../../../../../models/test-data';
import { AllMethodsStep } from '../../../../../decorators/test-steps';
import BaseTestData from '../../../../../base/base-test-data';
import claimantDefendantPartyTypes from '../../../../../constants/claimant-defendant-party-types.ts';
import DefaultJudgmentPageFactory from '../../../../../pages/exui/claimant-defendant-solicitor/default-judgment/default-judgment-page-factory.ts';

@AllMethodsStep()
export default class DefaultJudgmentSpecActions extends BaseTestData {
  private defaultJudgmentFactory: DefaultJudgmentPageFactory;

  constructor(defaultJudgmentPageFactory: DefaultJudgmentPageFactory, testData: TestData) {
    super(testData);
    this.defaultJudgmentFactory = defaultJudgmentPageFactory;
  }

  async defendantDetails() {
    const { defendantDetailsSpecPage } = this.defaultJudgmentFactory;
    await defendantDetailsSpecPage.verifyContent(this.ccdCaseData);
    await defendantDetailsSpecPage.selectDefendant(super.defendant1PartyType);
    await defendantDetailsSpecPage.submit();
  }

  async defendantDetails1v2() {
    const { defendantDetailsSpec1v2Page } = this.defaultJudgmentFactory;
    await defendantDetailsSpec1v2Page.verifyContent(this.ccdCaseData);
    await defendantDetailsSpec1v2Page.selectBothDefendants();
    await defendantDetailsSpec1v2Page.submit();
  }

  async showCertifyStatement() {
    const { showCertifyStatementSpecPage } = this.defaultJudgmentFactory;
    await showCertifyStatementSpecPage.verifyContent(this.ccdCaseData);
    await showCertifyStatementSpecPage.acceptCPR();
    await showCertifyStatementSpecPage.submit();
  }

  async showCertifyStatementMultipleDefendants() {
    const { showCertifyStatementSpecMultipleDefendantPage } = this.defaultJudgmentFactory;
    await showCertifyStatementSpecMultipleDefendantPage.verifyContent(this.ccdCaseData);
    await showCertifyStatementSpecMultipleDefendantPage.acceptCPR();
    await showCertifyStatementSpecMultipleDefendantPage.submit();
  }

  async claimPartialPayment() {
    const { claimPartialPaymentPage } = this.defaultJudgmentFactory;
    await claimPartialPaymentPage.verifyContent(
      this.ccdCaseData,
      claimantDefendantPartyTypes.COMPANY,
    );
    await claimPartialPaymentPage.selectNoPartialPayment();
    await claimPartialPaymentPage.submit();
  }

  async claimPartialPayment1v2() {
    const { claimPartialPayment1v2Page } = this.defaultJudgmentFactory;
    await claimPartialPayment1v2Page.verifyContent(this.ccdCaseData);
    await claimPartialPayment1v2Page.selectNoPartialPayment();
    await claimPartialPayment1v2Page.submit();
  }

  async fixedCostsOnEntry() {
    const { fixedCostsOnEntryPage } = this.defaultJudgmentFactory;
    await fixedCostsOnEntryPage.verifyContent(this.ccdCaseData);
    await fixedCostsOnEntryPage.selectYesClaimFixedCosts();
    await fixedCostsOnEntryPage.submit();
  }

  async paymentBySetDate() {
    const { paymentBreakdownPage } = this.defaultJudgmentFactory;
    await paymentBreakdownPage.verifyContent(this.ccdCaseData);
    await paymentBreakdownPage.submit();

    const { paymentTypePage } = this.defaultJudgmentFactory;
    await paymentTypePage.verifyContent(this.ccdCaseData, super.defendant1PartyType);
    await paymentTypePage.selectSetPaymentDate();
    await paymentTypePage.submit();

    const { paymentSetDatePage } = this.defaultJudgmentFactory;
    await paymentSetDatePage.verifyContent(this.ccdCaseData, super.defendant1PartyType);
    await paymentSetDatePage.setPaymentDate();
    await paymentSetDatePage.submit();
  }

  async paymentWithRepayment1v2() {
    const { paymentBreakdown1v2Page } = this.defaultJudgmentFactory;
    await paymentBreakdown1v2Page.verifyContent(this.ccdCaseData);
    await paymentBreakdown1v2Page.submit();

    const { paymentType1v2Page } = this.defaultJudgmentFactory;
    await paymentType1v2Page.verifyContent(this.ccdCaseData);
    await paymentType1v2Page.selectRepaymentPlan();
    await paymentType1v2Page.submit();

    const { repaymentInformation1v2Page } = this.defaultJudgmentFactory;
    await repaymentInformation1v2Page.verifyContent(this.ccdCaseData);
    await repaymentInformation1v2Page.regularPaymentsAmount();
    await repaymentInformation1v2Page.selectEveryMonth();
    await repaymentInformation1v2Page.firstInstalmentDate();
    await repaymentInformation1v2Page.submit();
  }

  async paymentSetDate1v2() {
    const { paymentSetDate1v2Page } = this.defaultJudgmentFactory;
    await paymentSetDate1v2Page.verifyContent(this.ccdCaseData);
    await paymentSetDate1v2Page.setPaymentDate();
    await paymentSetDate1v2Page.submit();
  }

  async repaymentInformation() {
    const { repaymentInformationPage } = this.defaultJudgmentFactory;
    await repaymentInformationPage.verifyContent(
      this.ccdCaseData,
      claimantDefendantPartyTypes.COMPANY,
    );
    await repaymentInformationPage.regularPaymentsAmount();
    await repaymentInformationPage.selectMonthlyRepayments();
    await repaymentInformationPage.firstInstalmentDate();
    await repaymentInformationPage.submit();
  }

  async submitDefaultJudgment() {
    const { submitDefaultJudgmentPage } = this.defaultJudgmentFactory;
    await submitDefaultJudgmentPage.verifyContent(this.ccdCaseData);
    await submitDefaultJudgmentPage.submit();
  }

  async confirmDefaultJudgmentSpec() {
    const { confirmDefaultJudgmentSpecPage } = this.defaultJudgmentFactory;
    await confirmDefaultJudgmentSpecPage.verifyContent(this.ccdCaseData);
    await confirmDefaultJudgmentSpecPage.submit();
  }
}
