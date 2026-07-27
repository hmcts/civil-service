import BaseTestData from '../../../../base/base-test-data';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import TestData from '../../../../models/test-utils/test-data';
import AddAdditionalDatesPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/add-additional-dates/add-additional-dates-page-factory';

@AllMethodsStep()
export default class AddUnavailableDatesActions extends BaseTestData {
  private addAdditionalDatesPageFactory: AddAdditionalDatesPageFactory;

  constructor(addAdditionalDatesPageFactory: AddAdditionalDatesPageFactory, testData: TestData) {
    super(testData);
    this.addAdditionalDatesPageFactory = addAdditionalDatesPageFactory;
  }

  async addAdditionalDates() {
    const { addAdditionalDatesPage } = this.addAdditionalDatesPageFactory;
    await addAdditionalDatesPage.verifyContent(this.ccdCaseData);
    await addAdditionalDatesPage.addSingleDateAndDateRange();
    await addAdditionalDatesPage.submit();
  }

  async submitAddAdditionalDates() {
    const { submitAddAdditionalDatesPage } = this.addAdditionalDatesPageFactory;
    await submitAddAdditionalDatesPage.verifyContent(this.ccdCaseData);
    await submitAddAdditionalDatesPage.submit();
  }

  async confirmAddAdditionalDates() {
    const { confirmAddAdditionalDatesPage } = this.addAdditionalDatesPageFactory;
    await confirmAddAdditionalDatesPage.verifyContent(this.ccdCaseData);
    await confirmAddAdditionalDatesPage.submit();
  }
}
