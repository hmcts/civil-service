

/**
 * update notification template
 */


update dbs.dashboard_notifications_templates set description_En = replace(description_En, 'Review the new bundle</a>.</p>', 'Review the new bundle</a></p>') where template_name in ('Notice.AAA6.CP.Bundle.Updated.Claimant', 'Notice.AAA6.CP.Bundle.Updated.Defendant');
update dbs.dashboard_notifications_templates set description_Cy = replace(description_Cy, 'Adolygu’r bwndel newydd</a>.</p>', 'Adolygu’r bwndel newydd</a></p>') where template_name in ('Notice.AAA6.CP.Bundle.Updated.Claimant', 'Notice.AAA6.CP.Bundle.Updated.Defendant');

