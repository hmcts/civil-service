import BaseRequest from '../base/base-request';
import { TOTP } from 'totp-generator';
import { AllMethodsStep } from '../decorators/test-steps';
import config from '../config/config';
import RequestOptions from '../models/api/request-options';
import urls from '../config/urls';
import User from '../models/user';

let civilS2sToken: string;

export default function ServiceAuthProviderRequests<
  TBase extends abstract new (...args: any[]) => BaseRequest,
>(Base: TBase) {
  @AllMethodsStep()
  abstract class ServiceAuthProviderRequests extends Base {
    protected async getRequestHeaders({ accessToken }: User) {
      const civilS2sToken = await this.fetchCivilS2sToken();
      return {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accessToken}`,
        ServiceAuthorization: civilS2sToken,
      };
    }

    private async fetchCivilS2sToken() {
      if (!civilS2sToken) {
        console.log('Fetching s2s token...');
        const url = `${urls.authProviderApi}/lease`;
        const requestOptions: RequestOptions = {
          method: 'POST',
          body: {
            microservice: config.s2s.microservice,
            oneTimePassword: TOTP.generate(config.s2s.secret).otp,
          },
        };
        const responseText = await super.retryRequestText(url, requestOptions);
        console.log('s2s token fetched successfully');
        civilS2sToken = responseText;
      }
      return civilS2sToken;
    }
  }

  return ServiceAuthProviderRequests;
}
