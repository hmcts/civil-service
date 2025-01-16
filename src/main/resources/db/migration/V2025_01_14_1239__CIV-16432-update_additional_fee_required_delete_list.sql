
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant", "Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant", "Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant", "Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant"}'
                    where name = 'Scenario.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant';
