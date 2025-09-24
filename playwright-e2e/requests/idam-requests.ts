import BaseRequest from '../base/base-request';
import urls from '../config/urls';
import { AllMethodsStep } from '../decorators/test-steps';
import RequestOptions from '../models/api/request-options';
import User from '../models/user';

@AllMethodsStep()
export default class IdamRequests extends BaseRequest {
  async getAccessToken({ name, email, password }: User): Promise<string> {
    console.log(`Fetching access token for user: ${name}...`);
    const url = `${urls.idamApi}/loginUser`;
    const requestOptions: RequestOptions = {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      params: { username: email, password: password },
      method: 'POST',
    };
    const responseJson = await super.retryRequestJson(url, requestOptions);
    console.log(`Access token for user: ${name} fetched successfully`);
    return responseJson.access_token;
  }

  async getUserId({ accessToken, email, name }: User): Promise<string> {
    console.log(`Fetching User ID for user: ${name}`);
    const url = `${urls.idamApi}/o/userinfo`;
    const requestOptions: RequestOptions = {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Authorization: `Bearer ${accessToken}`,
      },
      method: 'GET',
    };
    const responseJson = await super.retryRequestJson(url, requestOptions);
    console.log(`User ID for user: ${name} fetched successfully`);
    return responseJson.uid;
  }
}
