/**
 * Update scenario
 */
UPDATE dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InvalidRef.Claimant", "Notice.AAA6.CP.HearingFee.HWF.InfoRequired.Claimant", "Notice.AAA6.CP.HearingFee.HWF.ReviewUpdate.Claimant","Notice.AAA6.CP.HearingFee.HWF.InfoRequired"}'
                                                            WHERE name = 'Scenario.AAA6.CP.HearingFee.HWF.Rejected';

UPDATE dbs.scenario set notifications_to_delete = '{"Notice.AAA6.CP.HearingFee.HWF.AppliedFor.Claimant","Notice.AAA6.CP.HearingFee.HWF.InfoRequired"}'
                                                            WHERE name = 'Scenario.AAA6.CP.HearingFee.HWF.PartRemission';
