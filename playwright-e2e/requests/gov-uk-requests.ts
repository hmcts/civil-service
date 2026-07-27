import BaseRequest from '../base/base-request';
import urls from '../config/urls';
import { AllMethodsStep } from '../decorators/test-steps';

@AllMethodsStep()
export default class GovUKRequests extends BaseRequest {
  async fetchBankHolidays() {
    console.log('Fetching UK bank holidays...');
    const url = `${urls.govUK}/bank-holidays.json`;
    const responseJson = await super.requestJson(
      url,
      {},
      {
        verifyResponse: async (responseJson) => {
          await super.expectResponseJsonToHaveProperty('england-and-wales', responseJson);
        },
      },
    );
    console.log('Bank holidays fetched successfully');
    return responseJson;
  }
}
