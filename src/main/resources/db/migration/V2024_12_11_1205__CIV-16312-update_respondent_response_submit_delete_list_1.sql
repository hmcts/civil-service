
update dbs.scenario set notifications_to_delete = '{"Notice.AAA6.GeneralApps.NonUrgentApplicationUncloaked.Respondent", "Notice.AAA6.GeneralApps.UrgentApplicationUncloaked.Respondent"}'
                    where name = 'Scenario.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent';
