const urls = {
  manageCase: process.env.URL,
  authProviderApi: process.env.SERVICE_AUTH_PROVIDER_API_BASE_URL,
  ccdDataStore: process.env.CCD_DATA_STORE_URL,
  dmStore: process.env.DM_STORE_URL,
  idamWeb: process.env.IDAM_WEB_URL,
  idamApi: process.env.IDAM_API_URL,
  civilService: process.env.CIVIL_SERVICE_URL,
  generalApplication: process.env.CIVIL_GENERAL_APPLICATIONS_URL,
  waTaskMgmtApi: process.env.WA_TASK_MGMT_URL,
  caseAssignmentService: process.env.AAC_API_URL,
  orchestratorService: process.env.CIVIL_ORCHESTRATOR_SERVICE_URL,
  paymentApi: process.env.PAYMENT_API_URL,
  wiremockService: process.env.WIRE_MOCK_SERVICE_URL,
  govUK: 'https://www.gov.uk',
};

export const getDomain = (url: string) => {
  return new URL(url).host;
};

export default urls;
