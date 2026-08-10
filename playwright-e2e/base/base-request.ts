import { APIRequestContext, APIResponse } from '@playwright/test';
import ExpectOptions from '../models/api/expect-options';
import RequestOptions from '../models/api/request-options';
import { expect } from '../playwright-fixtures';
import { BoxedDetailedStep } from '../decorators/test-steps';
import ResponseDataType from '../constants/test-utils/response-data-type';
import * as responseOptions from '../models/api/response-options';
import NonRetryableError from '../errors/non-retryable-error';

const classKey = 'BaseRequest';

export default abstract class BaseRequest {
  private requestContext: APIRequestContext;
  private MAX_RETRY_TIMEOUT = 30000;

  constructor(requestContext: APIRequestContext) {
    this.requestContext = requestContext;
  }

  private sleep(ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  private getResponseData(
    response: APIResponse,
    responseDataType: ResponseDataType,
  ): Promise<any | string | null> {
    switch (responseDataType) {
      case ResponseDataType.JSON:
        return response.json();
      case ResponseDataType.TEXT:
        return response.text();
      default:
        return null;
    }
  }

  private async _request(
    url: string,
    {
      headers = { 'Content-Type': 'application/json' },
      body: body,
      method = 'GET',
      params,
    }: RequestOptions = {},
    responseType = ResponseDataType.NONE,
    { expectedStatus = 200, verifyResponse, statusErrorMessage }: responseOptions._ResponseOptions = {},
  ): Promise<APIResponse | any | string> {
    const response = await this.requestContext.fetch(url, {
      method,
      data: body ? JSON.stringify(body) : undefined,
      headers,
      params,
    });
    const responseData = await this.getResponseData(response, responseType);
    await this.expectStatus(
      expectedStatus,
      response.status(),
      response.url(),
      response.statusText(),
      statusErrorMessage ? await statusErrorMessage(responseData ?? response, {
        url: response.url(),
        status: response.status(),
        headers: response.headers(),
        expectedStatus
      }) : undefined,
    );
    if (verifyResponse)
      await verifyResponse(responseData ?? response, {
        status: response.status(),
        headers: response.headers(),
      });
    return responseData ?? response;
  }

  private async _retryRequest(
    url: string,
    requestOptions: RequestOptions = {},
    responseDataType = ResponseDataType.NONE,
    {
      expectedStatus = 200,
      retries = 2,
      retryTimeInterval = 5000,
      verifyResponse,
      statusErrorMessage
    }: responseOptions._RetryResponseOptions = {},
  ): Promise<APIResponse | any | string> {
    if (retryTimeInterval > this.MAX_RETRY_TIMEOUT) {
      retryTimeInterval = this.MAX_RETRY_TIMEOUT;
    }
    while (retries >= 0) {
      try {
        const response = await this._request(url, requestOptions, responseDataType, {
          expectedStatus,
          verifyResponse,
          statusErrorMessage
        });
        return response;
      } catch (error: any) {
        if (NonRetryableError.is(error)) {
          throw error;
        }
        if (retries <= 0) throw error;
        console.log(
          `${error.message.split('\n')[0]}, retrying in ${retryTimeInterval / 1000} seconds (Retries left: ${retries})`,
        );
        retries--;
        await this.sleep(retryTimeInterval);
      }
    }
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async request(
    url: string,
    requestOptions?: RequestOptions,
    responseOptions?: responseOptions.ResponseOptions,
  ) {
    return (await this._request(
      url,
      requestOptions,
      ResponseDataType.NONE,
      responseOptions,
    )) as APIResponse;
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async retryRequest(
    url: string,
    requestOptions?: RequestOptions,
    retryResponseOptions?: responseOptions.RetryResponseOptions,
  ) {
    return (await this._retryRequest(
      url,
      requestOptions,
      ResponseDataType.NONE,
      retryResponseOptions,
    )) as APIResponse;
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async requestJson(
    url: string,
    requestOptions?: RequestOptions,
    responseJsonOptions?: responseOptions.ResponseJsonOptions,
  ): Promise<any> {
    return (await this._request(
      url,
      requestOptions,
      ResponseDataType.JSON,
      responseJsonOptions,
    )) as any;
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async retryRequestJson(
    url: string,
    requestOptions?: RequestOptions,
    retryResponseJsonOptions?: responseOptions.RetryResponseJsonOptions,
  ): Promise<any> {
    return (await this._retryRequest(
      url,
      requestOptions,
      ResponseDataType.JSON,
      retryResponseJsonOptions,
    )) as any;
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async requestText(
    url: string,
    requestOptions?: RequestOptions,
    responseTextOptions?: responseOptions.ResponseTextOptions,
  ): Promise<string> {
    return (await this._request(
      url,
      requestOptions,
      ResponseDataType.TEXT,
      responseTextOptions,
    )) as string;
  }

  @BoxedDetailedStep(classKey, 'url')
  protected async retryRequestText(
    url: string,
    requestOptions?: RequestOptions,
    retryResponseTextOptions?: responseOptions.RetryResponseTextOptions,
  ): Promise<string> {
    return (await this._retryRequest(
      url,
      requestOptions,
      ResponseDataType.TEXT,
      retryResponseTextOptions,
    )) as string;
  }

  @BoxedDetailedStep(classKey, 'actualStatus', 'expectedStatus')
  private async expectStatus(
    expectedStatus: number,
    actualStatus: number,
    url: string,
    statusText: string,
    message?: string,
    nonRetryable = false,
  ) {
    try {
      expect(
        actualStatus,
        message ?? `Expected status: ${expectedStatus}, actual status: ${actualStatus}, ` +
          `message: ${statusText}, url: ${url}`,
      ).toBe(expectedStatus);
    } catch (error: any) {
      if (nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey, 'keyPath')
  protected async expectResponseJsonToHaveProperty(
    keyPath: string,
    responseJson: any,
    options: ExpectOptions = {},
  ) {
    try {
      expect(
        responseJson,
        options.message ??
          `Expected response json to have property '${keyPath.split('.').join(' => ')}'`,
      ).toHaveProperty(keyPath);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey, 'keyPath')
  protected async expectResponseJsonToNotHaveProperty(
    keyPath: string,
    responseJson: any,
    options: ExpectOptions = {},
  ) {
    try {
      expect(
        responseJson,
        options.message ??
          `Expected response json to have property '${keyPath.split('.').join(' => ')}'`,
      ).not.toHaveProperty(keyPath);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey, 'keyPath', 'value')
  protected async expectResponseJsonToHavePropertyValue(
    keyPath: string,
    value: any,
    responseJson: any,
    options: ExpectOptions = {},
  ) {
    try {
      expect(
        responseJson,
        options.message ??
          `Expected response json to have property '${keyPath.split('.').join(' => ')}'`,
      ).toHaveProperty(keyPath, value);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey)
  protected async expectResponseJsonToContain(
    partialObject: any,
    responseJson: any,
    options: ExpectOptions = {},
  ) {
    try {
      expect(responseJson, options.message).toMatchObject(partialObject);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey)
  protected async expectResponseJsonArrayToContain(
    partialArray: any[],
    responseJsonArray: any[],
    options: ExpectOptions = {},
  ) {
    const expectedArrayItems = partialArray.map((item) =>
      item !== null && typeof item === 'object' && !Array.isArray(item)
        ? expect.objectContaining(item)
        : item,
    );
    try {
      expect(responseJsonArray, options.message).toEqual(
        expect.arrayContaining(expectedArrayItems),
      );
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey)
  protected async expectResponseJsonToEqual(
    object: any,
    responseJson: any,
    options: ExpectOptions = {},
  ) {
    try {
      expect(responseJson, options.message).toEqual(object);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }

  @BoxedDetailedStep(classKey, 'text', 'responseText')
  protected async expectResponseTextToEqual(
    text: string,
    responseText: string,
    options: ExpectOptions = {},
  ) {
    try {
      expect(text, options.message).toEqual(responseText);
    } catch (error: any) {
      if (options.nonRetryable) {
        throw NonRetryableError.mark(error);
      }
      throw error;
    }
  }
}
