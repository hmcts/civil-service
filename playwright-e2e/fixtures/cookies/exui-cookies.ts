import urls, { getDomain } from '../../config/urls';
import Cookie from '../../models/test-utils/cookie';

export const generateAcceptExuiCookies = (userId: string): Cookie[] => [
  {
    name: `hmcts-exui-cookies-${userId}-mc-accepted`,
    value: 'true',
    domain: getDomain(urls.manageCase),
    path: '/',
  },
];
