import { expect } from 'chai';
import { mergeCaseData } from '../lib/civil.mjs';
import { caseRowsQuery, compareCaseRows } from '../lib/db.mjs';
import { resolveConfig } from '../lib/env.mjs';
import { selectCases } from '../lib/manifest.mjs';
import { diffSnapshots, diffViews, fieldHashes, missingTasks, summariseEvents } from '../lib/snapshot.mjs';
import { totp } from '../lib/totp.mjs';
import { selectProfiles } from '../profiles.mjs';

describe('cutover harness', () => {
  describe('totp', () => {
    it('matches the RFC 6238 SHA-1 test vector', () => {
      // Secret "12345678901234567890" in base32; at T=59s the 8 digit code is 94287082.
      expect(totp('GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ', { timeMs: 59_000, digits: 8 })).to.equal('94287082');
      expect(totp('GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ', { timeMs: 59_000 })).to.equal('287082');
    });
  });

  describe('mergeCaseData', () => {
    it('merges objects deeply and replaces arrays', () => {
      const merged = mergeCaseData(
        { party: { name: 'A', type: 'COMPANY' }, list: [1, 2] },
        { party: { name: 'B' }, list: [3] }
      );
      expect(merged).to.deep.equal({ party: { name: 'B', type: 'COMPANY' }, list: [3] });
    });
  });

  describe('snapshots', () => {
    const snapshot = overrides => ({
      state: 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT',
      securityClassification: 'PUBLIC',
      fields: fieldHashes({ applicant1: { partyName: 'Test Inc' }, SearchCriteria: { x: 1 } }),
      events: ['2026-11-26T10:00:00 CREATE_CLAIM_SPEC:PENDING_CASE_ISSUED'],
      triggers: ['CHANGE_SOLICITOR_EMAIL', 'INITIATE_GENERAL_APPLICATION'],
      eventStarts: { CHANGE_SOLICITOR_EMAIL: 200, INITIATE_GENERAL_APPLICATION: 200 },
      ...overrides
    });

    it('ignores SearchCriteria, which the runtime rebuilds on every read', () => {
      expect(fieldHashes({ a: 1, SearchCriteria: {} })).to.have.all.keys('a');
    });

    it('finds no difference in an identical snapshot', () => {
      expect(diffSnapshots(snapshot(), snapshot())).to.deep.equal([]);
    });

    it('reports changed and missing fields by name', () => {
      const after = snapshot({ fields: fieldHashes({ applicant1: { partyName: 'Other' } }) });
      const [difference] = diffSnapshots(snapshot({ fields: fieldHashes({ applicant1: {}, respondent1: {} }) }), after);
      expect(difference).to.deep.include({ check: 'fields', changed: ['applicant1'], missingAfter: ['respondent1'] });
    });

    it('allows new events on the end only when asked, and never a changed history', () => {
      const extra = snapshot({ events: [...snapshot().events, '2026-11-27T10:00:00 NOC_REQUEST:AWAITING'] });
      expect(diffSnapshots(snapshot(), extra).map(d => d.check)).to.deep.equal(['events']);
      expect(diffSnapshots(snapshot(), extra, { allowNewEvents: true })).to.deep.equal([]);
      const rewritten = snapshot({ events: ['2026-11-26T10:00:00 OTHER:PENDING_CASE_ISSUED'] });
      expect(diffSnapshots(snapshot(), rewritten, { allowNewEvents: true }).map(d => d.check)).to.deep.equal(['events']);
    });

    it('reports triggers and event start results that changed', () => {
      const after = snapshot({
        triggers: ['CHANGE_SOLICITOR_EMAIL'],
        eventStarts: { CHANGE_SOLICITOR_EMAIL: 422 }
      });
      const checks = diffSnapshots(snapshot(), after);
      expect(checks.find(d => d.check === 'triggers').missingAfter).to.deep.equal(['INITIATE_GENERAL_APPLICATION']);
      expect(checks.find(d => d.check === 'eventStarts').changed)
        .to.deep.equal([{ eventId: 'CHANGE_SOLICITOR_EMAIL', before: 200, after: 422 }]);
    });

    it('requires access to be refused after cutover exactly where it was before', () => {
      const before = { admin: snapshot(), defendantSolicitor: { access: false, status: 403 } };
      before.admin.access = true;
      const after = { admin: { access: false, status: 404 }, defendantSolicitor: { access: false, status: 404 } };
      expect(diffViews(before, after)).to.deep.equal([{ role: 'admin', check: 'access', before: true, after: false }]);
    });

    it('orders history oldest first whatever order data store returns it in', () => {
      expect(summariseEvents([
        { id: 'B', state_id: 'S2', created_date: '2026-11-26T11:00:00' },
        { id: 'A', state_id: 'S1', created_date: '2026-11-26T10:00:00' }
      ])).to.deep.equal(['2026-11-26T10:00:00 A:S1', '2026-11-26T11:00:00 B:S2']);
    });

    it('finds tasks missing after cutover by id and type', () => {
      const before = [{ id: '1', type: 'JudgeDecideOnApplication' }, { id: '2', type: 'ReviewApplication' }];
      expect(missingTasks(before, [{ id: '1', type: 'JudgeDecideOnApplication' }])).to.deep.equal([before[1]]);
    });
  });

  describe('database comparison', () => {
    const ccdRow = {
      reference: '1234567890123456', jurisdiction: 'CIVIL', case_type_id: 'CIVIL', state: 'PENDING_CASE_ISSUED',
      security_classification: 'PUBLIC', version: 3, created_date: 'c', last_modified: 'm',
      last_state_modified_date: 's', resolved_ttl: null, data_hash: 'd', supplementary_data_hash: 'sd',
      event_count: 2, events_hash: 'e', latest_event_id: 'CREATE_CLAIM_SPEC', case_revision: null
    };
    const civilRow = {
      ...ccdRow, case_revision: '1000000002', max_event_revision: 2, event_revisions_sequential: true
    };

    it('passes a faithful copy with the SDK renumbering', () => {
      expect(compareCaseRows(ccdRow, civilRow)).to.deep.equal([]);
    });

    it('reports copied columns that differ', () => {
      expect(compareCaseRows(ccdRow, { ...civilRow, data_hash: 'x', state: 'OTHER' }).map(m => m.field))
        .to.deep.equal(['state', 'data_hash']);
    });

    it('reports a case revision that is not the highest event revision plus the offset', () => {
      expect(compareCaseRows(ccdRow, { ...civilRow, case_revision: '2' }).map(m => m.field))
        .to.deep.equal(['case_revision']);
    });

    it('reports a case missing from either side', () => {
      expect(compareCaseRows(ccdRow, { reference: null })[0].problem).to.equal('missing from civil-service');
    });

    it('refuses anything but 16 digit references and plain schema names in its SQL', () => {
      expect(() => caseRowsQuery('ccd', ['1; drop table x'])).to.throw('Invalid case reference');
      expect(() => caseRowsQuery('ccd; --', ['1234567890123456'])).to.throw('Invalid schema name');
      expect(caseRowsQuery('ccd', ['1234567890123456'])).to.contain('ccd.case_event');
    });
  });

  describe('configuration', () => {
    it('builds preview URLs from the PR number', () => {
      const config = resolveConfig({ CUTOVER_ENV: 'preview', CUTOVER_PR_ID: '8496' });
      expect(config.urls.dataStore)
        .to.equal('https://ccd-data-store-api-civil-service-pr-8496.preview.platform.hmcts.net');
      expect(config.urls.waTaskManagement).to.equal('');
    });

    it('uses internal URLs in AAT and lets each be overridden', () => {
      const config = resolveConfig({ CUTOVER_ENV: 'aat', CUTOVER_CIVIL_SERVICE_URL: 'http://localhost:4000/' });
      expect(config.urls.dataStore).to.equal('http://ccd-data-store-api-aat.service.core-compute-aat.internal');
      expect(config.urls.civilService).to.equal('http://localhost:4000');
    });

    it('refuses prod and preview without a PR number', () => {
      expect(() => resolveConfig({ CUTOVER_ENV: 'prod' })).to.throw('CUTOVER_ENV must be one of');
      expect(() => resolveConfig({ CUTOVER_ENV: 'preview' })).to.throw('CUTOVER_PR_ID');
    });
  });

  describe('selection', () => {
    it('refuses unknown profiles', () => {
      expect(() => selectProfiles(['nope'])).to.throw('Unknown CUTOVER_PROFILE_IDS');
      expect(selectProfiles([]).map(profile => profile.profileId))
        .to.deep.equal(['claim-pending-issue', 'claim-issued', 'ga-awaiting-judge']);
    });

    it('checks only cases that seeded', () => {
      const manifest = { cases: [{ profileId: 'a', status: 'seeded' }, { profileId: 'b', status: 'failed' }] };
      expect(selectCases(manifest).map(seeded => seeded.profileId)).to.deep.equal(['a']);
      expect(() => selectCases(manifest, ['c'])).to.throw('not in the manifest');
    });
  });
});
