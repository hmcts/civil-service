import { request, retry } from './http.mjs';
import { createGeneralApplication, createSpecClaimPages, gaPaymentUpdate, specClaimPaymentUpdate } from './fixtures.mjs';

export const JURISDICTION = 'CIVIL';
export const CIVIL = 'CIVIL';
export const GENERAL_APPLICATION = 'GENERALAPPLICATION';

// Fields a page validation returns that the next page must not send back, as civil-ccd-definition's API tests do.
const VALIDATE_RESPONSE_FIELDS_TO_DROP = [
  'SearchCriteria',
  'notificationSummary',
  'smallClaimsFlightDelayToggle',
  'smallClaimsFlightDelay'
];

/** Deep-merges modifications into current, replacing arrays, as civil-ccd-definition's API tests do. */
export function mergeCaseData(current, modifications) {
  const merged = { ...current };
  for (const [key, value] of Object.entries(modifications || {})) {
    const existing = current[key];
    merged[key] = existing && typeof existing === 'object' && !Array.isArray(existing) && value
      && typeof value === 'object' && !Array.isArray(value)
      ? mergeCaseData(existing, value)
      : value;
  }
  return merged;
}

export class CivilApi {
  constructor(config, auth) {
    this.config = config;
    this.auth = auth;
  }

  async caseTypeUrl(user, caseType) {
    const { userId } = await this.auth.user(user);
    return `${this.config.urls.dataStore}/caseworkers/${userId}/jurisdictions/${JURISDICTION}/case-types/${caseType}`;
  }

  async startEvent(user, caseType, eventId, caseId) {
    const base = await this.caseTypeUrl(user, caseType);
    const casePath = caseId ? `/cases/${caseId}` : '';
    return request('GET', `${base}${casePath}/event-triggers/${eventId}/token`, {
      headers: await this.auth.headers(user),
      expectStatus: [200]
    });
  }

  async validatePage(user, caseType, eventId, pageId, data, eventToken, caseId) {
    const base = await this.caseTypeUrl(user, caseType);
    const body = { data, event: { id: eventId }, event_data: data, event_token: eventToken };
    if (caseId) {
      body.case_reference = caseId;
    }
    const response = await request('POST', `${base}/validate?pageId=${eventId}${pageId}`, {
      headers: await this.auth.headers(user),
      body,
      expectStatus: [200]
    });
    const returned = { ...(response.data || {}) };
    VALIDATE_RESPONSE_FIELDS_TO_DROP.forEach(field => delete returned[field]);
    return returned;
  }

  async submitEvent(user, caseType, eventId, data, eventToken, caseId) {
    const base = await this.caseTypeUrl(user, caseType);
    const url = caseId ? `${base}/cases/${caseId}/events` : `${base}/cases`;
    return request('POST', url, {
      headers: await this.auth.headers(user),
      body: { data, event: { id: eventId }, event_data: data, event_token: eventToken },
      expectStatus: [201]
    });
  }

  async getCase(user, caseType, caseId) {
    const base = await this.caseTypeUrl(user, caseType);
    return request('GET', `${base}/cases/${caseId}`, { headers: await this.auth.headers(user), expectStatus: [200] });
  }

  /** The case as ExUI shows it, including the events this user can trigger. */
  async getCaseForDisplay(user, caseType, caseId) {
    const { userId } = await this.auth.user(user);
    return request('GET', `${this.config.urls.dataStore}/aggregated/caseworkers/${userId}/jurisdictions/`
      + `${JURISDICTION}/case-types/${caseType}/cases/${caseId}`, {
      headers: await this.auth.headers(user),
      expectStatus: [200]
    });
  }

  async getCaseEvents(user, caseType, caseId) {
    const base = await this.caseTypeUrl(user, caseType);
    return request('GET', `${base}/cases/${caseId}/events`, { headers: await this.auth.headers(user), expectStatus: [200] });
  }

  async searchCases(user, caseType, query) {
    return request('POST', `${this.config.urls.dataStore}/searchCases?ctid=${caseType}`, {
      headers: await this.auth.headers(user),
      body: query,
      expectStatus: [200]
    });
  }

  async globalSearch(user, caseReference) {
    return request('POST', `${this.config.urls.dataStore}/globalSearch`, {
      headers: await this.auth.headers(user),
      body: {
        searchCriteria: { CCDJurisdictionIds: [JURISDICTION], caseReferences: [String(caseReference)] },
        maxReturnRecordCount: 10,
        startRecordNumber: 1
      },
      expectStatus: [200]
    });
  }

  /** Case data as civil-service reads it through data store, with its business process. */
  async civilServiceCase(user, caseId) {
    return request('GET', `${this.config.urls.civilService}/testing-support/case/${caseId}`, {
      headers: await this.auth.headers(user),
      expectStatus: [200]
    });
  }

  /**
   * Waits until the case's business process has finished in Camunda and, if given, the case is in expectedState.
   * Fails fast on a Camunda incident.
   */
  async waitForBusinessProcess(user, caseId, { expectedState, ga = false } = {}) {
    const path = ga ? 'business-process/ga' : 'business-process';
    return retry(async () => {
      const response = await request('GET', `${this.config.urls.civilService}/testing-support/case/${caseId}/${path}`, {
        headers: await this.auth.headers(user),
        expectStatus: [200]
      });
      if (response.incidentMessage) {
        const error = new Error(`Camunda incident for case ${caseId}: ${response.incidentMessage}`);
        error.fatal = true;
        throw error;
      }
      const process = response.businessProcess;
      if (process && process.status !== 'FINISHED') {
        throw new Error(`Case ${caseId} business process ${process.camundaEvent} is ${process.status}`);
      }
      if (expectedState && response.ccdState !== expectedState) {
        throw new Error(`Case ${caseId} is in ${response.ccdState}, waiting for ${expectedState}`);
      }
      return response;
    }, this.config.poll);
  }

  async assignCase(user, caseId, caseRole) {
    return request('POST', `${this.config.urls.civilService}/testing-support/assign-case/${caseId}/${caseRole}`, {
      headers: await this.auth.headers(user),
      body: {},
      expectStatus: [200, 409]
    });
  }

  async paymentUpdate(path, body, user) {
    const headers = user
      ? await this.auth.headers(user)
      : { ServiceAuthorization: `Bearer ${await this.auth.civilServiceToken()}` };
    return request('PUT', `${this.config.urls.civilService}${path}`, { headers, body, expectStatus: [200] });
  }

  /**
   * Creates an LR v LR specified small claim through CREATE_CLAIM_SPEC, page by page as ExUI would.
   * Returns the case reference once the claim is in PENDING_CASE_ISSUED and its business process has finished.
   */
  async createSpecClaim() {
    const { applicantSolicitor, defendantSolicitor } = this.config.users;
    const eventId = 'CREATE_CLAIM_SPEC';
    const start = await this.startEvent(applicantSolicitor, CIVIL, eventId);
    let data = {};
    for (const [pageId, pageData] of Object.entries(createSpecClaimPages(applicantSolicitor, defendantSolicitor))) {
      data = mergeCaseData(data, pageData);
      data = mergeCaseData(data, await this.validatePage(applicantSolicitor, CIVIL, eventId, pageId, data, start.token));
    }
    const submitStart = await this.startEvent(applicantSolicitor, CIVIL, eventId);
    const created = await this.submitEvent(applicantSolicitor, CIVIL, eventId, data, submitStart.token);
    const caseId = String(created.id);
    await this.waitForBusinessProcess(applicantSolicitor, caseId, { expectedState: 'PENDING_CASE_ISSUED' });
    return caseId;
  }

  /** Pays the claim fee and gives the defendant's solicitor their case role, so the claim is issued. */
  async issueSpecClaim(caseId) {
    const { applicantSolicitor, defendantSolicitor } = this.config.users;
    await this.paymentUpdate('/service-request-update-claim-issued', specClaimPaymentUpdate(caseId), applicantSolicitor);
    await this.waitForBusinessProcess(applicantSolicitor, caseId);
    await this.assignCase(defendantSolicitor, caseId, 'RESPONDENTSOLICITORONE');
    await this.waitForBusinessProcess(applicantSolicitor, caseId, { expectedState: 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT' });
  }

  /**
   * The claimant's solicitor applies, without notice, on the claim. Returns the GA case reference once it is paid
   * and awaiting a judge's decision, which is the state that creates work allocation's JudgeDecideOnApplication task.
   */
  async createGeneralApplication(parentCaseId) {
    const { applicantSolicitor } = this.config.users;
    const eventId = 'INITIATE_GENERAL_APPLICATION';
    const start = await this.startEvent(applicantSolicitor, CIVIL, eventId, parentCaseId);
    await this.submitEvent(applicantSolicitor, CIVIL, eventId, createGeneralApplication(), start.token, parentCaseId);
    await this.waitForBusinessProcess(applicantSolicitor, parentCaseId);
    await this.waitForBusinessProcess(applicantSolicitor, parentCaseId, { ga: true });

    const parent = await this.civilServiceCase(applicantSolicitor, parentCaseId);
    const applications = parent.claimantGaAppDetails || [];
    const gaCaseId = applications.at(-1)?.value?.caseLink?.CaseReference;
    if (!gaCaseId) {
      throw new Error(`No general application found on claim ${parentCaseId}`);
    }
    await this.waitForBusinessProcess(applicantSolicitor, gaCaseId, { expectedState: 'AWAITING_APPLICATION_PAYMENT' });
    await this.paymentUpdate('/service-request-update', gaPaymentUpdate(gaCaseId));
    await this.waitForBusinessProcess(applicantSolicitor, gaCaseId, {
      expectedState: 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION'
    });
    return String(gaCaseId);
  }

  /**
   * Notice of Change: nocSolicitor's organisation takes over the defendant's representation by answering the
   * challenge question, then the case's respondent1OrganisationPolicy names their organisation.
   */
  async noticeOfChange(caseId) {
    const { nocSolicitor, admin, applicantSolicitor } = this.config.users;
    const aac = this.config.urls.caseAssignment;
    const headers = await this.auth.headers(nocSolicitor);
    const answers = [{ question_id: 'clientName', value: 'Sir John Doe' }];
    await request('GET', `${aac}/noc/noc-questions?case_id=${caseId}`, { headers, expectStatus: [200] });
    await request('POST', `${aac}/noc/verify-noc-answers`, { headers, body: { case_id: caseId, answers }, expectStatus: [200] });
    await request('POST', `${aac}/noc/noc-requests`, { headers, body: { case_id: caseId, answers }, expectStatus: [201] });
    await this.waitForBusinessProcess(applicantSolicitor, caseId);
    const details = await this.getCase(admin, CIVIL, caseId);
    const orgId = details.case_data?.respondent1OrganisationPolicy?.Organisation?.OrganisationID;
    if (orgId !== nocSolicitor.orgId) {
      throw new Error(`After NoC, respondent1OrganisationPolicy is ${orgId}, expected ${nocSolicitor.orgId}`);
    }
    return { organisationId: orgId };
  }

  /** Work allocation tasks for the case that the tasks user can see. */
  async tasks(caseId) {
    const { token } = await this.auth.user(this.config.users.tasks);
    const response = await request('POST', `${this.config.urls.waTaskManagement}/task`, {
      headers: { Authorization: `Bearer ${token}`, ServiceAuthorization: `Bearer ${await this.auth.xuiToken()}` },
      body: {
        search_parameters: [
          { key: 'caseId', operator: 'IN', values: [String(caseId)] },
          { key: 'jurisdiction', operator: 'IN', values: [JURISDICTION] },
          { key: 'state', operator: 'IN', values: ['assigned', 'unassigned'] }
        ]
      },
      expectStatus: [200]
    });
    return (response.tasks || []).map(task => ({
      id: task.id,
      type: task.type,
      name: task.name,
      state: task.task_state
    }));
  }
}

