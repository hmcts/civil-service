/**
 * welsh amends
 */
/**
 * example 3
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Byddwch yn cael help i dalu’r ', 'Byddwch yn cael help gyda’r ffi ') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant' ;
/**
 * item 4 - removing gwneud
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwFRejected.Applicant';
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.InvalidRef.Applicant';
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.MoreInfoRequired.Applicant';
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant';
UPDATE dbs.dashboard_notifications_templates SET title_cy = replace(title_cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.FeePaid.Applicant';
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi gwneud', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.FullRemission.Applicant';
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'ffi sef', 'ffi') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwFRequested.Applicant';
/**
 * item 8
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Talu ffi’r gwneud cais ychwanegol', 'Talu’r ffi ychwanegol i wneud cais') WHERE template_name  = 'Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant' ;

/**
 * item 9 and 14
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'sy’ ', 'sy’n ') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant' ;


