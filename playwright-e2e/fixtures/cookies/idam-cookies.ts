import urls, { getDomain } from '../../config/urls';
import Cookie from '../../models/cookie';

export const acceptIdamCookies: Cookie[] = [
  {
    name: 'seen_cookie_message',
    value: 'yes',
    domain: getDomain(urls.idamWeb),
    path: '/',
    secure: true,
  },
  {
    name: 'cookies_preferences_set',
    value: 'true',
    domain: getDomain(urls.idamWeb),
    path: '/',
    secure: true,
  },
  {
    name: 'cookies_policy',
    value: '{"essential":true,"analytics":true,"apm":true}',
    domain: getDomain(urls.idamWeb),
    path: '/',
    secure: true,
  },
];
