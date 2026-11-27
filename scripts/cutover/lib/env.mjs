/**
 * Where the harness runs and as whom. Every URL and user can be overridden with an environment variable,
 * and the defaults are the ones civil-service's Jenkins pipeline and civil-ccd-definition's API tests use.
 */
const ENVIRONMENTS = ['preview', 'aat', 'perftest'];

function stripTrailingSlash(value) {
  return value ? value.replace(/\/+$/, '') : '';
}

function firstPresent(...values) {
  return values.find(value => value !== undefined && value !== null && value !== '');
}

function internalUrl(service, name) {
  return `http://${service}-${name}.service.core-compute-${name}.internal`;
}

function defaultUrls(name, prId) {
  if (name === 'preview') {
    if (!prId) {
      throw new Error('CUTOVER_ENV=preview needs CUTOVER_PR_ID (the civil-service PR number)');
    }
    const host = service => `https://${service}-civil-service-pr-${prId}.preview.platform.hmcts.net`;
    return {
      civilService: `https://civil-service-pr-${prId}.preview.platform.hmcts.net`,
      dataStore: host('ccd-data-store-api'),
      caseAssignment: host('manage-case-assignment'),
      // Civil's preview doesn't run work allocation, so the task check is skipped unless this is set.
      waTaskManagement: '',
      idamApi: 'https://idam-api.aat.platform.hmcts.net',
      s2s: internalUrl('rpe-service-auth-provider', 'aat')
    };
  }
  return {
    civilService: internalUrl('civil-service', name),
    dataStore: internalUrl('ccd-data-store-api', name),
    caseAssignment: internalUrl('aac-manage-case-assignment', name),
    waTaskManagement: internalUrl('wa-task-management-api', name),
    idamApi: `https://idam-api.${name}.platform.hmcts.net`,
    s2s: internalUrl('rpe-service-auth-provider', name)
  };
}

export function resolveConfig(env = process.env) {
  const name = (env.CUTOVER_ENV || 'aat').toLowerCase();
  if (!ENVIRONMENTS.includes(name)) {
    throw new Error(`CUTOVER_ENV must be one of ${ENVIRONMENTS.join(', ')}; got ${name}`);
  }
  const prId = firstPresent(env.CUTOVER_PR_ID, env.CHANGE_ID);
  const defaults = defaultUrls(name, prId);
  const password = env.DEFAULT_PASSWORD;

  const user = (key, email, orgId) => ({
    email: firstPresent(env[`CUTOVER_${key}_EMAIL`], email),
    password: firstPresent(env[`CUTOVER_${key}_PASSWORD`], password),
    orgId: firstPresent(env[`CUTOVER_${key}_ORG_ID`], orgId)
  });

  return {
    name,
    prId: prId || null,
    urls: {
      civilService: stripTrailingSlash(firstPresent(env.CUTOVER_CIVIL_SERVICE_URL, defaults.civilService)),
      dataStore: stripTrailingSlash(firstPresent(env.CUTOVER_CCD_DATA_STORE_URL, defaults.dataStore)),
      caseAssignment: stripTrailingSlash(firstPresent(env.CUTOVER_AAC_URL, defaults.caseAssignment)),
      waTaskManagement: stripTrailingSlash(firstPresent(env.CUTOVER_WA_TASK_MGMT_URL, defaults.waTaskManagement)),
      idamApi: stripTrailingSlash(firstPresent(env.CUTOVER_IDAM_API_URL, defaults.idamApi)),
      s2s: stripTrailingSlash(firstPresent(env.CUTOVER_S2S_URL, defaults.s2s)),
      elasticsearch: stripTrailingSlash(env.CUTOVER_ELASTICSEARCH_URL || '')
    },
    secrets: {
      idamClientSecret: env.CCD_API_GATEWAY_IDAM_CLIENT_SECRET,
      civilServiceS2s: env.S2S_SECRET,
      xuiS2s: env.XUI_S2S_SECRET
    },
    users: {
      // The organisations civil-ccd-definition's API tests use outside Demo.
      applicantSolicitor: user('APPLICANT_SOLICITOR', 'hmcts.civil+organisation.1.solicitor.1@gmail.com', 'Q1KOKP2'),
      defendantSolicitor: user('DEFENDANT_SOLICITOR', 'hmcts.civil+organisation.2.solicitor.1@gmail.com', '79ZRSOU'),
      // Takes over the defendant's representation in the Notice of Change check.
      nocSolicitor: user('NOC_SOLICITOR', 'civil.damages.claims+organisation.1.solicitor.1@gmail.com', '0FA7S8S'),
      admin: user('ADMIN', 'civil-admin@mailnesia.com'),
      tasks: user('TASKS_USER', 'ctsc_admin@justice.gov.uk')
    },
    outputDir: env.CUTOVER_OUTPUT_DIR || 'build/cutover',
    manifestFile: env.CUTOVER_MANIFEST_FILE || `${env.CUTOVER_OUTPUT_DIR || 'build/cutover'}/seed-manifest.json`,
    profileIds: (env.CUTOVER_PROFILE_IDS || '').split(',').map(id => id.trim()).filter(Boolean),
    poll: {
      attempts: parseInt(env.CUTOVER_POLL_ATTEMPTS || '40', 10),
      intervalMs: parseInt(env.CUTOVER_POLL_INTERVAL_MS || '15000', 10)
    }
  };
}

export function requireSecrets(config, names) {
  const envNames = {
    idamClientSecret: 'CCD_API_GATEWAY_IDAM_CLIENT_SECRET',
    civilServiceS2s: 'S2S_SECRET',
    xuiS2s: 'XUI_S2S_SECRET'
  };
  const missing = names.filter(name => !config.secrets[name]).map(name => envNames[name]);
  if (!config.users.applicantSolicitor.password) {
    missing.push('DEFAULT_PASSWORD');
  }
  if (missing.length > 0) {
    throw new Error(`Missing environment variables: ${missing.join(', ')}. See scripts/cutover/README.md`);
  }
}
