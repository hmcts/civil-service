import { createHash } from 'node:crypto';

/*
 * A snapshot is what one user sees of a case through data store: state, data, history and the events they can
 * start. The seed step takes one per case and user before cutover; verify-cases takes another afterwards and
 * compares them, so every check is "as it was before", not a hand-kept expectation.
 */

// Fields the decentralised runtime adds or rebuilds on every read, so they can't match a centralised read.
// SearchCriteria: the SDK derives it, with stable ids, for each case it returns to data store.
export const DEFAULT_IGNORED_FIELDS = ['SearchCriteria'];

function stableStringify(value) {
  if (Array.isArray(value)) {
    return `[${value.map(stableStringify).join(',')}]`;
  }
  if (value && typeof value === 'object') {
    return `{${Object.keys(value).sort().map(key => `${JSON.stringify(key)}:${stableStringify(value[key])}`).join(',')}}`;
  }
  return JSON.stringify(value);
}

export function hash(value) {
  return createHash('sha256').update(stableStringify(value)).digest('hex');
}

export function fieldHashes(data, ignoredFields = DEFAULT_IGNORED_FIELDS) {
  const hashes = {};
  for (const key of Object.keys(data || {}).sort()) {
    if (!ignoredFields.includes(key)) {
      hashes[key] = hash(data[key]);
    }
  }
  return hashes;
}

/**
 * The case's history, oldest first, as data store's events endpoint returns it (its "id" is the event id).
 */
export function summariseEvents(events) {
  const list = Array.isArray(events) ? events : events?.auditEvents || [];
  return [...list]
    .sort((a, b) => String(a.created_date).localeCompare(String(b.created_date)))
    .map(event => `${event.created_date} ${event.id}:${event.state_id ?? ''}`);
}

export function summariseTriggers(caseForDisplay) {
  return (caseForDisplay?.triggers || []).map(trigger => trigger.id).sort();
}

export function takeSnapshot({ caseDetails, caseForDisplay, events }) {
  return {
    state: caseDetails.state,
    securityClassification: caseDetails.security_classification ?? null,
    fields: fieldHashes(caseDetails.case_data),
    events: summariseEvents(events),
    triggers: summariseTriggers(caseForDisplay)
  };
}

/**
 * Lists every way after differs from before. Events are compared as a prefix: events submitted after cutover
 * (verify-writes) are allowed on the end, but the migrated history must be unchanged.
 */
export function diffSnapshots(before, after, { allowNewEvents = false } = {}) {
  const differences = [];
  if (before.state !== after.state) {
    differences.push({ check: 'state', before: before.state, after: after.state });
  }
  if (before.securityClassification !== after.securityClassification) {
    differences.push({
      check: 'securityClassification',
      before: before.securityClassification,
      after: after.securityClassification
    });
  }

  const fieldNames = new Set([...Object.keys(before.fields), ...Object.keys(after.fields)]);
  const changed = [...fieldNames].sort().filter(name => before.fields[name] !== after.fields[name]);
  if (changed.length > 0) {
    differences.push({
      check: 'fields',
      missingAfter: changed.filter(name => !(name in after.fields)),
      addedAfter: changed.filter(name => !(name in before.fields)),
      changed: changed.filter(name => name in before.fields && name in after.fields)
    });
  }

  const historyKept = before.events.every((event, index) => after.events[index] === event);
  const sameLength = before.events.length === after.events.length;
  if (!historyKept || (!allowNewEvents && !sameLength) || after.events.length < before.events.length) {
    differences.push({ check: 'events', before: before.events, after: after.events });
  }

  const missingTriggers = before.triggers.filter(id => !after.triggers.includes(id));
  const addedTriggers = after.triggers.filter(id => !before.triggers.includes(id));
  if (missingTriggers.length > 0 || addedTriggers.length > 0) {
    differences.push({ check: 'triggers', missingAfter: missingTriggers, addedAfter: addedTriggers });
  }

  if (before.eventStarts && after.eventStarts) {
    const changedStarts = Object.keys(before.eventStarts)
      .filter(eventId => eventId in after.eventStarts && before.eventStarts[eventId] !== after.eventStarts[eventId])
      .map(eventId => ({ eventId, before: before.eventStarts[eventId], after: after.eventStarts[eventId] }));
    if (changedStarts.length > 0) {
      differences.push({ check: 'eventStarts', changed: changedStarts });
    }
  }
  return differences;
}

/** Compares every user's view; a user refused before must be refused after, and the other way round. */
export function diffViews(beforeViews, afterViews, options) {
  const differences = [];
  for (const role of Object.keys(beforeViews)) {
    const before = beforeViews[role];
    const after = afterViews[role];
    if (!after || before.access !== after.access) {
      differences.push({ role, check: 'access', before: before.access, after: after?.access ?? null });
    } else if (before.access) {
      differences.push(...diffSnapshots(before, after, options).map(difference => ({ role, ...difference })));
    }
  }
  return differences;
}

/** Tasks recorded before cutover that are no longer there, matched on task id and type. */
export function missingTasks(before, after) {
  return before.filter(task => !after.some(found => found.id === task.id && found.type === task.type));
}
