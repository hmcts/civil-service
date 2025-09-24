const defaultPassword = process.env.DEFAULT_PASSWORD;
const judgeDefaultPassword = process.env.JUDGE_DEFAULT_PASSWORD;
const iacDefaultPassword = process.env.IAC_DEFAULT_PASSWORD;
const defaultPasswordSystemUser = process.env.SYSTEM_USER_PASSWORD;
const courtToBeSelected = 'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL';
const courtToBeSelectedHmc = 'Nottingham County Court And Family Court - Canal Street - NG1 7EJ';

module.exports = {
  idamStub: {
    enabled: process.env.IDAM_STUB_ENABLED === 'true',
    url: 'http://localhost:5555'
  },
  url: {

//    for Demo
//    manageCase: process.env.URL || 'https://manage-case-int.demo.platform.hmcts.net',
//     //manageCase: process.env.URL || 'https://manage-case-wa-int.demo.platform.hmcts.net',
//    authProviderApi: process.env.SERVICE_AUTH_PROVIDER_API_BASE_URL || 'http://rpe-service-auth-provider-demo.service.core-compute-demo.internal',
//    ccdDataStore: process.env.CCD_DATA_STORE_URL || 'http://ccd-data-store-api-demo.service.core-compute-demo.internal',
//    dmStore:process.env.DM_STORE_URL || 'http://dm-store-demo.service.core-compute-demo.internal',
//    idamApi: process.env.IDAM_API_URL || 'https://idam-api.demo.platform.hmcts.net',
//    civilService: process.env.CIVIL_SERVICE_URL || 'http://civil-service-demo.service.core-compute-demo.internal',
//    waTaskMgmtApi: process.env.WA_TASK_MGMT_URL || 'http://wa-task-management-api-demo.service.core-compute-demo.internal'

//    for Preview
//    manageCase: 'https://xui-civil-ccd-pr-5627.preview.platform.hmcts.net',
//    authProviderApi: 'http://rpe-service-auth-provider-aat.service.core-compute-aat.internal',
//    ccdDataStore: 'https://ccd-data-store-api-civil-ccd-pr-5627.preview.platform.hmcts.net',
//    dmStore: 'http://dm-store-aat.service.core-compute-aat.internal',
//    idamApi: 'https://idam-api.aat.platform.hmcts.net',
//    civilService: 'https://civil-ccd-pr-5627.preview.platform.hmcts.net',
//    caseAssignmentService: 'http://manage-case-assignment-civil-ccd-pr-5627.preview.platform.hmcts.net',
//    generalApplication: 'https://ga-civil-ccd-pr-5627.preview.platform.hmcts.net',
//    orchestratorService: 'http://civil-orchestrator-service-aat.service.core-compute-aat.internal',

//    for AAT
//    manageCase:  'https://manage-case.aat.platform.hmcts.net/',
//    authProviderApi:  'http://rpe-service-auth-provider-aat.service.core-compute-aat.internal',
//    ccdDataStore: 'http://ccd-data-store-api-aat.service.core-compute-aat.internal',
//    dmStore:'http://dm-store-aat.service.core-compute-aat.internal',
//    idamApi:  'https://idam-api.aat.platform.hmcts.net',
//    civilService: 'http://civil-service-aat.service.core-compute-aat.internal',
//    waTaskMgmtApi: 'http://wa-task-management-api-aat.service.core-compute-aat.internal',
//    caseAssignmentService: 'http://aac-manage-case-assignment-aat.service.core-compute-aat.internal',
//    generalApplication: 'http://civil-general-applications-aat.service.core-compute-aat.internal',
//    orchestratorService: 'http://civil-orchestrator-service-aat.service.core-compute-aat.internal',

//    Default - leave below uncommented when merging
    manageCase: process.env.URL || 'http://localhost:3333',
    authProviderApi: process.env.SERVICE_AUTH_PROVIDER_API_BASE_URL || 'http://localhost:4502',
    ccdDataStore: process.env.CCD_DATA_STORE_URL || 'http://localhost:4452',
    dmStore: process.env.DM_STORE_URL || 'http://dm-store:8080',
    idamApi: process.env.IDAM_API_URL || 'http://localhost:5000',
    civilService: process.env.CIVIL_SERVICE_URL || 'http://localhost:4000',
    caseAssignmentService: process.env.AAC_API_URL || 'http://localhost:4454',
    generalApplication: process.env.CIVIL_GENERAL_APPLICATIONS_URL  || 'http://localhost:4550',
    orchestratorService: process.env.CIVIL_ORCHESTRATOR_SERVICE_URL || 'https://localhost:9090',
    waTaskMgmtApi: process.env.WA_TASK_MGMT_URL || 'http://wa-task-management-api-aat.service.core-compute-aat.internal',
    paymentApi: process.env.PAYMENT_API_URL || 'http://payment-api-aat.service.core-compute-aat.internal',
    wiremockService: process.env.WIREMOCK_URL || 'http://localhost:8765'
  },
  s2s: {
    microservice: 'civil_service',
    secret: process.env.S2S_SECRET || 'AABBCCDDEEFFGGHH'
  },
  s2sForXUI: {
    microservice: 'xui_webapp',
    secret: process.env.XUI_S2S_SECRET || 'AABBCCDDEEFFGGHH'
  },
  applicantSolicitorUser: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.1.solicitor.1@gmail.com',
    type: 'applicant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'B04IXE4' : 'Q1KOKP2'
  },
  applicantSolicitorUserForBulkClaim: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.1.solicitor.2@gmail.com',
    type: 'applicant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'B04IXE4' : 'Q1KOKP2'
  },
  defendantSolicitorUser: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.2.solicitor.1@gmail.com',
    type: 'defendant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'DAWY9LJ' : '79ZRSOU'
  },
  secondDefendantSolicitorUser: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.3.solicitor.1@gmail.com',
    type: 'defendant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'LCVTI1I' : 'H2156A0'
  },
  otherSolicitorUser1: {
    password: defaultPassword,
    email: 'civil.damages.claims+organisation.1.solicitor.1@gmail.com',
    type: 'defendant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'OZO586V' : '0FA7S8S'
  },
  otherSolicitorUser2: {
    password: defaultPassword,
    email: 'civil.damages.claims+organisation.2.solicitor.1@gmail.com',
    type: 'defendant_solicitor',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'DOSS3I2' : 'N5AFUXG'
  },
  adminUser: {
    password: defaultPassword,
    email: 'civil-admin@mailnesia.com',
    type: 'admin'
  },
  nbcUserWithRegionId1: {
    password: defaultPassword,
    email: 'nbc_admin_region1@justice.gov.uk',
    type: 'admin'
  },
  nbcUserWithRegionId2: {
    password: defaultPassword,
    email: 'nbc_admin_region2@justice.gov.uk',
    type: 'admin'
  },
  nbcUserWithRegionId4: {
    password: defaultPassword,
    email: 'nbc_admin_region4@justice.gov.uk',
    type: 'admin'
  },
  nbcUserLocal: {
    password: defaultPassword,
    email: 'nbc-team-leader@mailnesia.com',
    type: 'admin'
  },
  judgeUserWithRegionId1: {
    password: judgeDefaultPassword,
    email: 'DJ.Amy.Powell@ejudiciary.net',
    type: 'judge',
    roleCategory: 'JUDICIAL',
    regionId: '1'
  },
  circuitJudgeUserWithRegionId1: {
    password: judgeDefaultPassword,
    email: '4917924EMP-@ejudiciary.net',
    type: 'judge',
    roleCategory: 'JUDICIAL',
    regionId: '1'
  },
  judgeUser2WithRegionId4: {
    password: judgeDefaultPassword,
    email: '4924246EMP-@ejudiciary.net',
    type: 'judge',
    roleCategory: 'JUDICIAL',
    regionId: '4'
  },
  judgeUserWithRegionId1Local: {
    password: defaultPassword,
    email: 'judge-civil-02@example.com',
    type: 'judge',
    roleCategory: 'JUDICIAL',
    regionId: '1'
  },
  judgeUser2WithRegionId2: {
    password: judgeDefaultPassword,
    email: 'EMP42506@ejudiciary.net',
    type: 'judge',
    roleCategory: 'JUDICIAL',
    regionId: '2'
  },
  hearingCenterAdminLocal: {
    email: 'hearing-centre-admin-01@example.com',
    password: defaultPassword,
    type: 'hearing-center-admin',
    roleCategory: 'ADMIN',
    regionId: '1'
  },
  hearingCenterAdminWithRegionId1: {
    email: 'hearing_center_admin_reg1@justice.gov.uk',
    password: defaultPassword,
    type: 'hearing-center-admin',
    roleCategory: 'ADMIN',
    regionId: '1'
  },
  hearingCenterAdminWithRegionId2: {
    email: 'hearing_center_admin_reg2@justice.gov.uk',
    password: defaultPassword,
    type: 'hearing-center-admin',
    roleCategory: 'ADMIN',
    regionId: '1'
  },
  hearingCenterAdminWithRegionId4: {
    email: 'hearing_center_admin_region4@justice.gov.uk',
    password: defaultPassword,
    type: 'hearing-center-admin',
    roleCategory: 'ADMIN',
    regionId: '2'
  },
  tribunalCaseworkerWithRegionId12: {
    email: 'CIVIL_WA_func_test_demo_user7@justice.gov.uk',
    password: defaultPassword,
    type: 'tribunal-caseworker',
    roleCategory: 'LEGAL_OPERATIONS',
    regionId: '12'
  },
  tribunalCaseworkerWithRegionId4: {
    email: 'tribunal_legal_caseworker_reg4@justice.gov.uk',
    password: defaultPassword,
    type: 'tribunal-caseworker',
    roleCategory: 'LEGAL_OPERATIONS',
    regionId: '1'
  },
  ctscAdminUser: {
    email: 'ctsc_admin@justice.gov.uk',
    password: defaultPassword,
    type: 'tribunal-caseworker',
    roleCategory: 'CTSC',
    regionId: '1'
  },
  tribunalCaseworkerWithRegionId1Local: {
    email: 'tribunal-caseworker-01@example.com',
    password: defaultPassword,
    type: 'tribunal-caseworker',
    roleCategory: 'LEGAL_OPERATIONS',
    regionId: '1'
  },
  systemupdate: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.1.superuser@gmail.com',
    type: 'systemupdate'
  },
  systemUpdate2: {
    password: defaultPasswordSystemUser,
    email: 'civil-system-update@mailnesia.com',
    type: 'systemupdate',
  },
  definition: {
    jurisdiction: 'CIVIL',
    caseType: 'CIVIL' + (process.env.CCD_DEF_VERSION || '')
  },
  iacLeadershipJudge: {
    password: iacDefaultPassword,
    email: '330127EMP-@ejudiciary.net',
    type: 'judge',
    roleCategory: 'JUDICIAL'
  },
  iacLegalOpsUser: {
    password: iacDefaultPassword,
    email: 'CRD_func_test_demo_stcwuser1@justice.gov.uk',
    type: 'legalOps',
    roleCategory: 'LEGAL_OPERATIONS'
  },
  iacCtscTeamLeaderUser: {
    email: 'CRD_func_test_demo_ctsc_tl@justice.gov.uk',
    password: iacDefaultPassword,
    type: 'ctsc-team-leader',
    roleCategory: 'CTSC',
    regionId: 'none'
  },
  iacAdminUser: {
    password: iacDefaultPassword,
    email: 'CRD_func_test_demo_adm21@justice.gov.uk',
    type: 'admin',
    roleCategory: 'ADMIN'
  },
  iacAATAdminUser: {
    password: iacDefaultPassword,
    email: '	crd_func_test_aat_adm22@justice.gov.uk  ',
    type: 'admin',
    roleCategory: 'ADMIN'
  },
  nbcTeamLeaderWithRegionId4: {
    email: 'nbc_team_leader_region4@justice.gov.uk',
    password: defaultPassword,
    type: 'nbc-team-leader',
    roleCategory: 'NBC ADMIN',
    regionId: '4'
  },
  nbcTeamLeaderWithRegionId1: {
    email: 'nbc_team_lead_reg1@justice.gov.uk',
    password: defaultPassword,
    type: 'nbc-team-leader',
    roleCategory: 'NBC ADMIN',
    regionId: '1'
  },
  seniorTBCWWithRegionId4: {
    email: 'seniorcivil_tbcw_region4@justice.gov.uk',
    password: defaultPassword,
    type: 'senior-tribunal-caseworker',
    roleCategory: 'LEGAL_OPS',
    regionId: '4'
  },
  ctscTeamLeaderUser: {
    email: 'ctsc_team_leader_region4@justice.gov.uk',
    password: defaultPassword,
    type: 'hmcts-ctsc',
    roleCategory: 'CTSC',
    regionId: 'none'
  },
  staffUIAdmin: {
    email: 'staff-ui-admin@justice.gov.uk',
    password: defaultPassword,
    type: 'staff-admin',
    roleCategory: 'cwd-admin',
    regionId: 'none'
  },
  feePaidJudge: {
    email: '49932114EMP-@ejudiciary.net',
    password: judgeDefaultPassword,
    type: 'judge',
    roleCategory: 'JUDICIAL'
  },
  bulkClaimSystemUser: {
    password: defaultPassword,
    email: 'hmcts.civil+organisation.1.solicitor.1@gmail.com', // temporary email
    type: 'bulk_system_user',
    orgId: process.env.ENVIRONMENT === 'demo' ? 'B04IXE4' : 'Q1KOKP2'
  },
  applicantCitizenUser: {
    password: defaultPassword,
    email: 'civilmoneyclaimsdemo@gmail.com',
    type: 'claimant',
  },
  defendantCitizenUser2: {
    password: defaultPassword,
    email: `citizen.${new Date().getTime()}.${Math.random()}.user@gmail.com`,
    type: 'defendant',
  },
  defendantLRCitizenUser:{
    password: defaultPassword,
    email: 'cuiuseraat@gmail.com',
    type: 'defendant',
  },
  waTaskIds: {
    judgeUnspecDJTask :'summaryJudgmentDirections',
    listingOfficerCaseProgressionTask: 'transferCaseOffline',
    scheduleAHearing: 'ScheduleHMCHearing',
    reviewSpecificAccessRequestJudiciary: 'reviewSpecificAccessRequestJudiciary',
    reviewSpecificAccessRequestLegalOps: 'reviewSpecificAccessRequestLegalOps',
    reviewSpecificAccessRequestAdmin: 'reviewSpecificAccessRequestAdmin',
    reviewSpecificAccessRequestCTSC: 'reviewSpecificAccessRequestCTSC',
    fastTrackDirections: 'FastTrackDirections',
    smallClaimDirections: 'SmallClaimsTrackDirections',
    legalAdvisorDirections: 'LegalAdvisorSmallClaimsTrackDirections',
    notSuitableSdo: 'transferCaseOfflineNotSuitableSDO',
    intermediateTrackDirections: 'allocateIntermediateTrack',
    multiTrackDirections: 'allocateMultiTrack',
    multiTrackOrderMadeReview: 'reviewOrder',
    transferCaseOffline: 'Transfer Case Offline'
  },
  TestOutputDir: process.env.E2E_OUTPUT_DIR || 'test-results/functional',
  TestForAccessibility: process.env.TESTS_FOR_ACCESSIBILITY === 'true',
  runningEnv: process.env.ENVIRONMENT,
  runWAApiTest: process.env.RUN_WA_API_TEST === 'true',
  runFailedTests: process.env.RUN_FAILED_AND_NOT_EXECUTED_TEST_FILES === 'true',
  claimantSolicitorOrgId: process.env.ENVIRONMENT === 'demo' ? 'B04IXE4' : 'Q1KOKP2',
  defendant1SolicitorOrgId: process.env.ENVIRONMENT === 'demo' ? 'DAWY9LJ' : '79ZRSOU',
  defendant2SolicitorOrgId: process.env.ENVIRONMENT === 'demo' ? 'LCVTI1I' : 'H2156A0',
  claimantSelectedCourt: courtToBeSelected,
  defendantSelectedCourt: courtToBeSelected,
  defendant2SelectedCourt: courtToBeSelected,
  djClaimantSelectedCourt: courtToBeSelected,
  liverpoolCourt: 'Liverpool Civil and Family Court - 35, Vernon Street, City Square - L2 2BX',
  sdoJudgeSelectedCourt: courtToBeSelected,
  localNoCTests: false,
  localMediationTests: false,
  claimantSelectedCourtHmc: courtToBeSelectedHmc,
  defendantSelectedCourtHmc: courtToBeSelectedHmc,
};
