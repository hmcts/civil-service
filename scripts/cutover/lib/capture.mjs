import { HttpError } from './http.mjs';
import { takeSnapshot } from './snapshot.mjs';
import { CIVIL } from './civil.mjs';

/** Which users' views of a case are snapshotted, by case type. */
export function snapshotUsers(config, caseType) {
  const { applicantSolicitor, defendantSolicitor, admin } = config.users;
  return caseType === CIVIL
    ? { applicantSolicitor, defendantSolicitor, admin }
    : { applicantSolicitor, admin };
}

/**
 * Starts each event the user is offered and records data store's answer: 200, or the status an about-to-start
 * callback refused it with. Starting an event runs its about-to-start callback but saves nothing.
 */
async function startEach(api, user, caseType, caseId, triggers) {
  const starts = {};
  for (const eventId of triggers) {
    try {
      await api.startEvent(user, caseType, eventId, caseId);
      starts[eventId] = 200;
    } catch (error) {
      if (!(error instanceof HttpError)) {
        throw error;
      }
      starts[eventId] = error.status;
    }
  }
  return starts;
}

/**
 * Each user's view of the case. A user data store refuses is recorded as such, so verify can check access is
 * refused the same way afterwards.
 */
export async function captureViews(api, config, caseType, caseId, { startEvents = true } = {}) {
  const views = {};
  for (const [role, user] of Object.entries(snapshotUsers(config, caseType))) {
    try {
      const caseDetails = await api.getCase(user, caseType, caseId);
      const caseForDisplay = await api.getCaseForDisplay(user, caseType, caseId);
      const events = await api.getCaseEvents(user, caseType, caseId);
      const view = { access: true, ...takeSnapshot({ caseDetails, caseForDisplay, events }) };
      if (startEvents) {
        view.eventStarts = await startEach(api, user, caseType, caseId, view.triggers);
      }
      views[role] = view;
    } catch (error) {
      if (error instanceof HttpError && [403, 404].includes(error.status)) {
        views[role] = { access: false, status: error.status };
      } else {
        throw error;
      }
    }
  }
  return views;
}
