import AxeCacheHelper from '../helpers/axe-cache-helper.ts';
import BankHolidaysHelper from '../helpers/bank-holidays-helper.ts';
import CookiesHelper from '../helpers/cookies-helper.ts';
import UserStateHelper from '../helpers/users-state-helper.ts';
import UserAssignedCasesHelper from '../helpers/user-assigned-cases-helper.ts';

const globalTeardownCI = () => {
  UserStateHelper.deleteAllUsersState();
  CookiesHelper.deleteAllCookies();
  AxeCacheHelper.deleteAllCache();
  BankHolidaysHelper.deleteBankHolidays();
  UserAssignedCasesHelper.deleteAllUsersAssignedCases();
};

globalTeardownCI();
