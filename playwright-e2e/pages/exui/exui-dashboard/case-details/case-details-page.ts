import BasePage from '../../../../base/base-page';
import config from '../../../../config/config';
import urls from '../../../../config/urls';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import { TruthyParams } from '../../../../decorators/truthy-params';
import CCDCaseData from '../../../../models/ccd/ccd-case-data';
import { CCDEvent } from '../../../../models/ccd/ccd-events';
import {
  components,
  getFormattedCaseId,
  getUnformattedCaseId,
  headings,
} from '../../exui-page/exui-content';
import ExuiPage from '../../exui-page/exui-page';
import {
  buttons,
  caseFlagsNoticeText,
  containers,
  dropdowns,
  successBannerText,
  tabs,
} from './case-details-content';

const classKey = 'CaseDetailsPage';

@AllMethodsStep()
export default class CaseDetailsPage extends ExuiPage(BasePage) {
  async verifyContent(caseData: CCDCaseData) {
    await super.runVerifications([
      super.verifyHeadings(caseData),
      super.expectSelector(tabs.summary.selector),
      super.expectSelector(tabs.caseFile.selector),
      super.expectSelector(tabs.claimDetails.selector),
      super.expectSelector(tabs.history.selector),
      // super.expectText(tabs.claimDocs.title),
      // super.expectSelector(tabs.paymentHistory.selector),
      // super.expectText(tabs.serviceRequest.title, { exact: true }),
      super.expectSelector(tabs.bundles.selector),
      super.expectSelector(tabs.caseFlags.selector),
      // super.expectLabel(dropdowns.nextStep.label),
    ]);
  }

  async verifySummaryContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.summary.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyCaseFileContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.caseFile.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyClaimDetailsContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.claimDetails.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyClaimDocumentsContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.claimDocs.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyPaymentHistoryContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.paymentHistory.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyBundlesContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.bundles.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async verifyCaseFlagsContent(caseData: CCDCaseData) {
    await super.clickByText(tabs.caseFlags.title);
    await super.runVerifications([], { useAxeCache: false });
  }

  async grabCaseNumber() {
    return getUnformattedCaseId(await super.getText(headings.caseNumber.selector));
  }

  @TruthyParams(classKey, 'caseId')
  async goToCaseDetails(caseId: number, { force }: { force: boolean } = { force: true }) {
    console.log(`Navigating to case with ccd case id: ${caseId}`);
    await super.goTo(`${urls.manageCase}/cases/case-details/${caseId}`, { force });
  }

  @TruthyParams(classKey, 'caseId')
  async retryGoToCaseDetails(caseId: number) {
    console.log(`Navigating to case with ccd case id: ${caseId}`);
    await super.retryGoTo(
      `${urls.manageCase}/cases/case-details/${caseId}`,
      () =>
        super.expectSelector(tabs.summary.selector, {
          timeout: config.playwright.shortExpectTimeout,
        }),
      undefined,
      { retries: 3, message: `Navigating to case with ccd case id: ${caseId}, trying again` },
    );
  }

  async chooseNextStep(ccdEvent: CCDEvent) {
    console.log(`Starting event: ${ccdEvent.name}`);
    await super.selectFromDropdown(ccdEvent.name, dropdowns.nextStep.selector);
    await super.clickBySelector(buttons.go.selector);
    super.setCCDEvent = ccdEvent;
  }

  async retryChooseNextStep(ccdEvent: CCDEvent) {
    console.log(`Starting event: ${ccdEvent.name}`);
    await super.retryAction(
      async () => {
        await super.retryReload(
          async () => {
            await super.expectSelector(dropdowns.nextStep.selector);
            await super.selectFromDropdown(ccdEvent.name, dropdowns.nextStep.selector, {
              timeout: 5_000,
            });
          },
          undefined,
          { retries: 1 },
        );
        await super.clickBySelector(buttons.go.selector);
      },
      async () => {
        await super.waitForPageToLoad();
        await super.expectNoSelector(tabs.summary.selector, {
          timeout: config.exui.pageSubmitTimeout,
        });
      },
      () => super.reload(),
      { retries: 3, message: `Starting event: ${ccdEvent.name} failed, trying again` },
    );
  }

  async chooseNextStepWithUrl(caseId: number, ccdEvent: CCDEvent) {
    console.log(`Starting event with url: ${ccdEvent.id}`);
    await super.goTo(
      `${urls.manageCase}/cases/case-details/${caseId}/trigger/${ccdEvent.id}/${ccdEvent.id}`,
    );
    super.setCCDEvent = ccdEvent;
  }

  async retryChooseNextStepWithUrl(caseId: number, ccdEvent: CCDEvent) {
    console.log(`Starting event with url: ${ccdEvent.id}`);
    await super.retryGoTo(
      `${urls.manageCase}/cases/case-details/${caseId}/trigger/${ccdEvent.id}/${ccdEvent.id}`,
      async () =>
        super.expectSelector(components.eventTrigger.selector, {
          timeout: config.exui.pageSubmitTimeout,
        }),
      undefined,
      { retries: 2, message: `Starting event with url: ${ccdEvent.id} failed, trying again` },
    );
    super.setCCDEvent = ccdEvent;
  }

  async verifySuccessEvent(caseId: number, ccdEvent: CCDEvent) {
    console.log(`Verifying success banner and event history: ${ccdEvent.name}`);
    await super.expectText(successBannerText(getFormattedCaseId(caseId), ccdEvent));
    await super.clickByText(tabs.history.title);
    await super.expectTableRowValue(ccdEvent.name, containers.eventHistory.selector, {
      rowNum: 1,
    });
  }

  async verifySuccessCaseFlagsEvent(activeCaseFlags: number, ccdEvent: CCDEvent) {
    console.log(`Verifying case flags notice and event history: ${ccdEvent.name}`);
    await super.expectText(caseFlagsNoticeText(activeCaseFlags), { exact: false });
    await super.clickByText(tabs.history.title);
    await super.expectTableRowValue(ccdEvent.name, containers.eventHistory.selector, {
      rowNum: 1,
    });
  }

  async submit() {
    throw new Error('Method not implemented.');
  }
}
