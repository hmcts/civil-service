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
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'Rhaid i chi dalu ffi gwneud cais ychwanegol', 'Rhaid i chi dalu ffi ychwanegol i wneud cais') WHERE template_name  = 'Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant' ;

/**
 * Dashboard notification 9
 */


/**
 * Dashboard notification 14
 */
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, 'weddill o', 'weddill sef') WHERE template_name  = 'Notice.AAA6.GeneralApps.HwF.PartRemission.Applicant' ;

