import AxeCacheHelper from '../helpers/axe-cache-helper';
import BankHolidaysHelper from '../helpers/bank-holidays-helper';
import CookiesHelper from '../helpers/cookies-helper';
import UserStateHelper from '../helpers/users-state-helper';
import UserAssignedCasesHelper from '../helpers/user-assigned-cases-helper';

const globalTeardownCI = () => {
  UserStateHelper.deleteAllUsersState();
  CookiesHelper.deleteAllCookies();
  AxeCacheHelper.deleteAllCache();
  BankHolidaysHelper.deleteBankHolidays();
  UserAssignedCasesHelper.deleteAllUsersAssignedCases();
};

globalTeardownCI();
