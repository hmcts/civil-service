/**
 * Update task title and description
 */
UPDATE dbs.dashboard_notifications_templates SET title_En = replace(title_En, 'The ${applicationFeeTypeEn} has been paid', 'The ${applicationFeeTypeEn} fee has been paid') WHERE template_name in ('Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant');
UPDATE dbs.dashboard_notifications_templates SET description_En = replace(description_En, '<p class="govuk-body">The ${applicationFeeTypeEn} has been paid in full.</p>', '<p class="govuk-body">The ${applicationFeeTypeEn} fee has been paid in full.</p>') WHERE template_name in ('Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant');
