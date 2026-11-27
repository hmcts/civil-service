import { request } from './http.mjs';
import { totp } from './totp.mjs';

/**
 * Caches IDAM and S2S tokens for the life of one script run, as civil-ccd-definition's API helpers do.
 */
export class Auth {
  constructor(config) {
    this.config = config;
    this.users = new Map();
    this.services = new Map();
  }

  async user(user) {
    if (!user.email || !user.password) {
      throw new Error(`No credentials for user ${user.email || '(unset)'}`);
    }
    if (!this.users.has(user.email)) {
      const form = new URLSearchParams({
        grant_type: 'password',
        username: user.email,
        password: user.password,
        client_id: 'ccd_gateway',
        client_secret: this.config.secrets.idamClientSecret,
        scope: 'openid profile roles'
      });
      const token = await request('POST', `${this.config.urls.idamApi}/o/token`, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: form.toString(),
        expectStatus: [200]
      });
      const info = await request('GET', `${this.config.urls.idamApi}/o/userinfo`, {
        headers: { Authorization: `Bearer ${token.access_token}` },
        expectStatus: [200]
      });
      this.users.set(user.email, { token: token.access_token, userId: info.uid });
    }
    return this.users.get(user.email);
  }

  async service(microservice, secret) {
    if (!secret) {
      throw new Error(`No S2S secret for ${microservice}`);
    }
    if (!this.services.has(microservice)) {
      const token = await request('POST', `${this.config.urls.s2s}/lease`, {
        body: { microservice, oneTimePassword: totp(secret) },
        expectStatus: [200]
      });
      this.services.set(microservice, typeof token === 'string' ? token.trim() : String(token));
    }
    return this.services.get(microservice);
  }

  civilServiceToken() {
    return this.service('civil_service', this.config.secrets.civilServiceS2s);
  }

  xuiToken() {
    return this.service('xui_webapp', this.config.secrets.xuiS2s);
  }

  /** Headers for data store, civil-service and case assignment calls made as user. */
  async headers(user, extra = {}) {
    const { token } = await this.user(user);
    return {
      Authorization: `Bearer ${token}`,
      ServiceAuthorization: `Bearer ${await this.civilServiceToken()}`,
      ...extra
    };
  }
}
