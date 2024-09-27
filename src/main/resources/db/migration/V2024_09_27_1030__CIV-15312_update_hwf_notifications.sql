/**
 * Update scenario
 */
UPDATE dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.HWF.Requested","Notice.AAA6.ClaimIssue.HWF.InfoRequired","Notice.AAA6.CP.HearingFee.HWF.InfoRequired"}'
                                                            WHERE name = 'Scenario.AAA6.ClaimIssue.HWF.PartRemission';

UPDATE dbs.scenario set notifications_to_delete = '{"Notice.AAA6.ClaimIssue.HWF.Requested","Notice.AAA6.ClaimIssue.HWF.Updated","Notice.AAA6.ClaimIssue.HWF.InvalidRef","Notice.AAA6.ClaimIssue.HWF.InfoRequired","Notice.AAA6.CP.HearingFee.HWF.InfoRequired"}'
                                                            WHERE name = 'Scenario.AAA6.ClaimIssue.HWF.Rejected';

UPDATE dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant","Notice.AAA6.ClaimIssue.HWF.InfoRequired","Notice.AAA6.CP.HearingFee.HWF.InfoRequired"}'
                                                            WHERE name = 'Scenario.AAA6.CP.HearingFee.HWF.PartRemission';
