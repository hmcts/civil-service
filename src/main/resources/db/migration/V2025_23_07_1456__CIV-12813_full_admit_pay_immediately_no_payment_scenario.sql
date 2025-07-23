/**
 * Update scenario
 */
UPDATE dbs.scenario set notifications_to_create = '{"Notice.AAA6.ClaimantIntent.FullAdmit.Claimant": ["respondent1PartyName", "fullAdmitPayImmediatelyPaymentAmount", "respondent1AdmittedAmountPaymentDeadlineEn", "respondent1AdmittedAmountPaymentDeadlineCy"]}'
WHERE name = 'Scenario.AAA6.ClaimantIntent.FullAdmit.Claimant';
