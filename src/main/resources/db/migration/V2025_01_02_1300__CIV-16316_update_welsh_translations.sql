/**
 * Dashboard notification 1
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Fe gewch diweddariad gyda gwybodaeth', 'Fe gewch ddiweddariad gyda gwybodaeth') WHERE template_name in ('Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Respondent', 'Notice.AAA6.GeneralApps.RespondentResponseSubmitted.Applicant');
/**
 * Dashboard notification 4
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Fe wnaethoch gais am help i dalu’r ffi gwneud', 'Fe wnaethoch gais am help i dalu’r ffi sef') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwFRequested.Applicant' ;
/**
 * Dashboard notification 8
 */
UPDATE dbs.dashboard_notifications_templates SET title_cy = replace(title_cy, 'Rhaid i chi dalu ffi gwneud cais ychwanegol', 'Rhaid i chi dalu ffi ychwanegol i wneud cais') WHERE template_name  = 'Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant' ;
/**
 * Dashboard notification 9
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Byddwch yn cael help gyda’r ffi gwneud', 'Byddwch yn cael help i dalu’r') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant' ;
/**
 * Dashboard notification 14
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'rhaid i chi dal dalu’r ffi sy’n weddill o', 'rhaid i chi dalu’r ffi sy’ weddill sef') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant' ;

