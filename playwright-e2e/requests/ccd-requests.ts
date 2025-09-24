import BaseRequest from '../base/base-request';
import config from '../config/config';
import urls from '../config/urls';
import { AllMethodsStep } from '../decorators/test-steps';
import RequestOptions from '../models/api/request-options';
import CCDCaseData from '../models/ccd/ccd-case-data';
import User from '../models/user';
import ServiceAuthProviderRequests from './service-auth-provider-requests';
import { CCDEvent } from '../models/ccd/ccd-events';
import CaseState from '../enums/case-state';

@AllMethodsStep({ methodNamesToIgnore: ['getCCDDataStoreBaseUrl'] })
export default class CCDRequests extends ServiceAuthProviderRequests(BaseRequest) {
  private getCCDDataStoreBaseUrl({ userId, role }: User) {
    return `${urls.ccdDataStore}/${role}s/${userId}/jurisdictions/${config.definition.jurisdiction}/case-types/${config.definition.caseType}`;
  }

  async fetchCCDCaseData(user: User, caseId: number) {
    console.log(`Fetching CCD case data, caseId: ${caseId}`);
    const url = `${this.getCCDDataStoreBaseUrl(user)}/cases/${caseId}`;
    const requestOptions: RequestOptions = {
      headers: await super.getRequestHeaders(user),
    };
    const responseJson = await super.retryRequestJson(url, requestOptions);
    console.log(`CCD case data fetched successfully, caseId: ${caseId}`);
    return { id: responseJson.id, ...responseJson.case_data };
  }

  async validatePageData(
    ccdEvent: CCDEvent,
    user: User,
    pageId: string,
    pageData: object,
    eventData: object,
    ccdEventToken: string,
  ): Promise<CCDCaseData> {
    console.log(`Asserting page: ${pageId} has valid data...`);
    const url = `${this.getCCDDataStoreBaseUrl(user)}/validate?pageId=${ccdEvent.id}${pageId}`;
    const requestOptions: RequestOptions = {
      headers: await this.getRequestHeaders(user),
      body: {
        data: pageData,
        event: { id: ccdEvent.id },
        event_data: eventData,
        event_token: ccdEventToken,
      },
      method: 'POST',
    };
    const responseJson = await super.retryRequestJson(url, requestOptions);
    console.log(`Page: ${pageId} has valid data`);
    return responseJson.data;
  }

  async startEvent(user: User, ccdEvent: CCDEvent, caseId?: number): Promise<string> {
    console.log(
      `Starting event: ${ccdEvent.id}` +
        (typeof caseId !== 'undefined' ? ` caseId: ${caseId}` : ''),
    );
    let url = this.getCCDDataStoreBaseUrl(user);
    if (caseId) {
      url += `/cases/${caseId}`;
    }
    url += `/event-triggers/${ccdEvent.id}/token`;

    const requestOptions: RequestOptions = {
      headers: await super.getRequestHeaders(user),
    };
    const response = await super.retryRequestJson(url, requestOptions, {
      verifyResponse: async (responseJson) => {
        await super.expectResponseJsonToHaveProperty('token', responseJson);
      },
    });
    console.log(`Event: ${ccdEvent.id} started successfully`);
    return response.token;
  }

  async submitEvent(
    user: User,
    ccdEvent: CCDEvent,
    expectedState: CaseState,
    eventData: any,
    ccdEventToken: string,
    caseId?: number,
  ): Promise<CCDCaseData> {
    console.log(
      `Submitting event: ${ccdEvent.id}` +
        (typeof caseId !== 'undefined' ? ` caseId: ${caseId}` : ''),
    );
    let url = `${this.getCCDDataStoreBaseUrl(user)}/cases`;
    if (caseId) {
      url += `/${caseId}/events`;
    }

    const requestOptions: RequestOptions = {
      headers: await super.getRequestHeaders(user),
      body: {
        data: eventData,
        event: { id: ccdEvent.id },
        event_token: ccdEventToken,
      },
      method: 'POST',
    };
    const responseJson = await super.retryRequestJson(url, requestOptions, {
      expectedStatus: 201,
      verifyResponse: async (responseJson) => {
        await super.expectResponseJsonToHavePropertyValue('state', expectedState, responseJson);
      },
    });
    const caseData: CCDCaseData = { id: responseJson.id, ...responseJson.case_data };
    console.log(`Event: ${ccdEvent.id} submitted successfully, caseId: ${caseData.id}`);
    return caseData;
  }
}
