import BasePage from '../../../../base/base-page';
import filePaths from '../../../../config/file-paths';
import { AllMethodsStep } from '../../../../decorators/test-steps';
import ExuiPage from '../../exui-page/exui-page';
import { buttons, inputs, subheadings } from './particulars-of-claim-content';

@AllMethodsStep()
export default class ParticularsOfClaimFragment extends ExuiPage(BasePage) {
  async verifyContent() {
    await super.runVerifications(
      [
        super.expectSubheading(subheadings.uploadDocuments),
        super.expectSubheading(subheadings.partiularsOfClaim),
        super.expectSubheading(subheadings.scheduleOfLoss),
        super.expectSubheading(subheadings.certificateOfSuitability),
      ],
      { runAxe: false },
    );
  }

  async uploadDocuments() {
    await super.clickBySelector(buttons.addPartiularsOfClaim.selector);
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadPartiularsOfClaim.selector);
    await super.clickBySelector(buttons.addMedicalReports.selector);
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadMedicalReports.selector);
    await super.clickBySelector(buttons.addScheduleOfLoss.selector);
    await super.retryUploadFile(filePaths.testPdfFile, inputs.uploadScheduleOfLoss.selector);
    await super.clickBySelector(buttons.addCertificateOfSuitability.selector);
    await super.retryUploadFile(
      filePaths.testPdfFile,
      inputs.uploadCertificateOfSuitability.selector,
    );
  }

  async submit() {
    await super.retryClickSubmit();
  }
}
