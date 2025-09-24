import BaseRequest from '../base/base-request';
import urls from '../config/urls';
import { AllMethodsStep } from '../decorators/test-steps';
import CaseRole from '../enums/case-role';
import RequestOptions from '../models/api/request-options';
import CCDCaseData, { UploadDocumentValue } from '../models/ccd/ccd-case-data';
import User from '../models/user';
import ServiceAuthProviderRequests from './service-auth-provider-requests';

@AllMethodsStep()
export default class CivilServiceRequests extends ServiceAuthProviderRequests(BaseRequest) {
  private testingSupportUrl = `${urls.civilService}/testing-support`;

  async uploadTestDocument(user: User): Promise<UploadDocumentValue> {
    console.log('Uploading test document...');
    const url = `${this.testingSupportUrl}/upload/test-document`;
    const requestOptions: RequestOptions = {
      headers: await super.getRequestHeaders(user),
      method: 'POST',
    };

    const responseJson = await super.retryRequestJson(url, requestOptions, {
      verifyResponse: async (responseJson) => {
        await super.expectResponseJsonToHaveProperty('document_url', responseJson);
        await super.expectResponseJsonToHaveProperty('document_binary_url', responseJson);
        await super.expectResponseJsonToHaveProperty('document_filename', responseJson);
      },
    });
    console.log('Test document uploaded sucessfully');
    return {
      document_url: responseJson.document_url,
      document_binary_url: responseJson.document_binary_url,
      document_filename: responseJson.document_filename,
    };
  }

  async waitForFinishedBusinessProcess(user: User, caseId: number) {
    console.log(`Waiting for business process to finish, caseId: ${caseId}`);
    const url = `${this.testingSupportUrl}/case/${caseId}/business-process`;
    const requestOptions: RequestOptions = {
      headers: await this.getRequestHeaders(user),
    };
    await super.retryRequestJson(url, requestOptions, {
      retries: 25,
      retryTimeInterval: 3000,
      verifyResponse: async (responseJson) => {
        await super.expectResponseJsonToHaveProperty('businessProcess', responseJson);
        const businessProcess = responseJson.businessProcess;
        await super.expectResponseJsonToHavePropertyValue(
          'businessProcess.status',
          'FINISHED',
          responseJson,
          {
            message:
              `Ongoing business process: ${businessProcess.camundaEvent}, caseId: ${caseId}, status: ${businessProcess.status},` +
              ` process instance: ${businessProcess.processInstanceId}, last finished activity: ${businessProcess.activityId}`,
          },
        );
        await super.expectResponseJsonToNotHaveProperty('incidentMessage', responseJson, {
          message: `Business process failed for case: ${caseId}, incident message: ${responseJson.incidentMessage}`,
        });
      },
    });
    console.log(`Business process successfully finished, caseId: ${caseId}`);
  }

  async updatePaymentForClaimIssue(user: User, serviceRequestDTO: any) {
    console.log(
      `Updating payment for claim issue, caseId: ${serviceRequestDTO.ccd_case_number}...`,
    );
    const url = `${urls.civilService}/service-request-update-claim-issued`;
    const requestOptions: RequestOptions = {
      headers: await super.getRequestHeaders(user),
      body: serviceRequestDTO,
      method: 'PUT',
    };
    await super.retryRequest(url, requestOptions);
    console.log(`Payment for claim issue successfully updated, caseId: ${serviceRequestDTO.id}`);
  }

  async assignCaseToDefendant(user: User, caseId: number, caseRole: CaseRole) {
    console.log(`Assigning role: ${caseRole} to user: ${user.name}, caseId: ${caseId}`);
    const url = `${this.testingSupportUrl}/assign-case/${caseId}/${caseRole}`;
    const requestOptions: RequestOptions = {
      headers: await this.getRequestHeaders(user),
      method: 'POST',
    };
    await super.retryRequest(url, requestOptions);
    console.log(`Role: ${caseRole} successfully assigned to user: ${user.name}, caseId: ${caseId}`);
  }

  async unassignUserFromCases(user: User, caseIds: number[]) {
    console.log(`Unassigning cases from user: ${user.name}...`);
    const url = `${this.testingSupportUrl}/unassign-user`;
    const requestOptions: RequestOptions = {
      headers: await this.getRequestHeaders(user),
      body: {
        caseIds,
      },
      method: 'POST',
    };
    await super.retryRequest(url, requestOptions);
    caseIds.forEach((caseId) =>
      console.log(`User: ${user.name} unassigned from case [${caseId}] successfully`),
    );
  }

  async updateCaseData(user: User, caseId: number, caseData: CCDCaseData) {
    console.log(`Updating case data, caseId: ${caseId}`);
    const url = `${this.testingSupportUrl}/case/${caseId}`;
    const requestOptions: RequestOptions = {
      headers: await this.getRequestHeaders(user),
      body: caseData,
      method: 'PUT',
    };
    await super.retryRequest(url, requestOptions);
    console.log(`Case data successfully updated, caseId: ${caseId}`);
  }
}
