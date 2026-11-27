import { CIVIL, GENERAL_APPLICATION } from './lib/civil.mjs';

/*
 * The cases the harness seeds before cutover. Each comes from a short API journey civil-ccd-definition's API
 * tests already prove, so seeding doesn't depend on deep journeys or the test repo.
 *
 * writes: what verify-writes does to the migrated case after routing.
 *   initiate-ga        the claimant's solicitor applies on the migrated claim (CIVIL write, Camunda, new GA case)
 *   notice-of-change   another organisation takes over the defendant's representation
 */
export const profiles = [
  {
    profileId: 'claim-pending-issue',
    caseType: CIVIL,
    description: 'Specified small claim created by a legal representative, fee not yet paid.',
    expectedState: 'PENDING_CASE_ISSUED',
    writes: []
  },
  {
    profileId: 'claim-issued',
    caseType: CIVIL,
    description: 'Specified small claim, paid and issued, defendant legal representative assigned.',
    expectedState: 'AWAITING_RESPONDENT_ACKNOWLEDGEMENT',
    writes: ['initiate-ga', 'notice-of-change']
  },
  {
    profileId: 'ga-awaiting-judge',
    caseType: GENERAL_APPLICATION,
    description: 'Paid application without notice on the issued claim, awaiting a judge (creates a WA task).',
    expectedState: 'APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION',
    parentProfileId: 'claim-issued',
    expectTask: 'JudgeDecideOnApplication',
    writes: []
  }
];

export function selectProfiles(profileIds) {
  if (profileIds.length === 0) {
    return profiles;
  }
  const unknown = profileIds.filter(id => !profiles.some(profile => profile.profileId === id));
  if (unknown.length > 0) {
    throw new Error(`Unknown CUTOVER_PROFILE_IDS: ${unknown.join(', ')}. Known: `
      + profiles.map(profile => profile.profileId).join(', '));
  }
  return profiles.filter(profile => profileIds.includes(profile.profileId));
}
