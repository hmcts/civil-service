import AxeCacheHelper from '../helpers/axe-cache-helper';
import BankHolidaysHelper from '../helpers/bank-holidays-helper';
import CookiesHelper from '../helpers/cookies-helper';
import UserAssignedCasesHelper from '../helpers/user-assigned-cases-helper';
import UserStateHelper from '../helpers/users-state-helper';
import { APIRequestContext, request } from 'playwright-core';
import { solicitorUsers } from '../config/users/exui-users';
import User from '../models/user';
import config from '../config/config';
import urls from '../config/urls';
import { TOTP } from 'totp-generator';

/*
This is last resort teardown for unassigning case if test execution gets interupted in local.
The logic here is also covered in the step method in the teardown project given by the following file path 
'playwright-e2e/tests/bootstrap/case-role-assignment/case-role-assignment.teardown.ts'
The reason why this code is duplicated is because when tests are being run locally an interruption caused by ctrl + c
Will not cause teardown projects to run in playwright, so this code has been duplicated to allow teardowns to still occur.
The code in the file path above will still be run, because it appears in playwrights reports as steps and logs however the code below will not.
*/

const getAccessToken = async (
  { name, email, password }: User,
  requestContext: APIRequestContext,
): Promise<string> => {
  console.log(`Fetching access token for user: ${name}...`);
  const url = `${urls.idamApi}/loginUser`;
  const requestOptions = {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    params: { username: email, password: password },
    method: 'POST',
  };
  const responseJson = await (await requestContext.fetch(url, requestOptions)).json();
  console.log(`Access token for user: ${name} fetched successfully`);
  return responseJson.access_token;
};

const fetchCivilS2sToken = async (requestContext: APIRequestContext) => {
  console.log('Fetching s2s token...');
  const url = `${urls.authProviderApi}/lease`;
  const requestOptions = {
    method: 'POST',
    data: {
      microservice: config.s2s.microservice,
      oneTimePassword: TOTP.generate(config.s2s.secret).otp,
    },
  };
  const responseText = await (await requestContext.fetch(url, requestOptions)).text();
  console.log('s2s token fetched successfully');
  return responseText;
};

const getRequestHeaders = async ({ accessToken }: User, civilS2sToken: string) => {
  return {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${accessToken}`,
    ServiceAuthorization: civilS2sToken,
  };
};

const unassignCaseFromUser = async (
  requestContext: APIRequestContext,
  user: User,
  civilS2sToken: string,
  caseIds: number[],
) => {
  console.log(`Unassigning cases from user: ${user.name}...`);
  const url = `${urls.civilService}/testing-support/unassign-user`;
  const requestOptions = {
    headers: await getRequestHeaders(user, civilS2sToken),
    data: {
      caseIds,
    },
    method: 'POST',
  };
  await requestContext.fetch(url, requestOptions);
  caseIds.forEach((caseId) =>
    console.log(`User: ${user.name} unassigned from case [${caseId}] successfully`),
  );
};

const unassignCases = async () => {
  if (config.unassignCases) {
    let civilS2sToken: string;
    for (const solicitorUser of solicitorUsers) {
      const assignedCases = await UserAssignedCasesHelper.getUserAssignedCases(solicitorUser);
      if (assignedCases) {
        try {
          const requestContext = await request.newContext();
          if (!solicitorUser.accessToken) {
            solicitorUser.accessToken = await getAccessToken(solicitorUser, requestContext);
          }
          if (!civilS2sToken) {
            civilS2sToken = await fetchCivilS2sToken(requestContext);
          }
          await unassignCaseFromUser(requestContext, solicitorUser, civilS2sToken, assignedCases);
        } catch (error) {
          console.log(`Unable to unassign cases for user: ${solicitorUser.name}`);
          console.log(error);
        }
      }
    }
  }
};

const globalTeardownLocal = async () => {
  await unassignCases();
  UserStateHelper.deleteAllUsersState();
  CookiesHelper.deleteAllCookies();
  AxeCacheHelper.deleteAllCache();
  BankHolidaysHelper.deleteBankHolidays();
  UserAssignedCasesHelper.deleteAllUsersAssignedCases();
};

export default globalTeardownLocal;
