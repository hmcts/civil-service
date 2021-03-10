/*global process*/

const defaultPassword = 'Password12!';

module.exports = {
  proxyServer: process.env.PROXY_SERVER,
  idamStub: {
    enabled: process.env.IDAM_STUB_ENABLED || false,
    url: 'http://localhost:5555'
  },
  url: {
    manageCase: process.env.URL || 'http://localhost:3333',
    authProviderApi: process.env.SERVICE_AUTH_PROVIDER_API_BASE_URL || 'http://localhost:4502',
    ccdDataStore: process.env.CCD_DATA_STORE_URL || 'http://localhost:4452',
    dmStore: process.env.DM_STORE_URL || 'http://dm-store:8080',
    idamApi: process.env.IDAM_API_URL || 'http://localhost:5000',
    unspecService: process.env.UNSPEC_SERVICE_URL || 'http://localhost:4000',
  },
  s2s: {
    microservice: 'unspec_service',
    secret: process.env.S2S_SECRET || 'AABBCCDDEEFFGGHH'
  },
  solicitorUser: {
    password: defaultPassword,
    email: 'civil.damages.claims+organisation.1.solicitor.1@gmail.com',
    type: 'solicitor'
  },
  defendantSolicitorUser: {
    password: defaultPassword,
    email: 'civil.damages.claims+organisation.2.solicitor.1@gmail.com',
    type: 'solicitor'
  },
  adminUser: {
    password: defaultPassword,
    email: 'civil-damages-admin@mailnesia.com',
    type: 'admin'
  },
  definition: {
    jurisdiction: 'CIVIL',
    caseType: 'UNSPECIFIED_CLAIMS',
  }
};
