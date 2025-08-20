/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">${applicant1PartyName} has accepted your offer to pay ${defendantAdmittedAmount},plus the claim fee and any fixed costs claimed,immediately in full and final settlement of the claim. Funds must be received in <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">their account</a> by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p><p class="govuk-body">If they don''t receive the money by then, they can request a County Court Judgment (CCJ).</p>',
    description_Cy = '<p class="govuk-body">Mae ${applicant1PartyName} wedi derbyn eich cynnig i dalu ${defendantAdmittedAmount},ynghyd â ffi’r hawliad ac unrhyw gostau sefydlog a hawlir,yn llawn ar unwaith fel setliad llawn a therfynol o’r hawliad.  Rhaid i’r arian fod yn <a href={VIEW_INFO_ABOUT_CLAIMANT} class="govuk-link">ei g/chyfrif</a> erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p><p class="govuk-body"> Os na fydd yr arian wedi cyrraedd erbyn hynny, gallant ofyn am Ddyfarniad Llys Sirol (CCJ).</p>'
WHERE template_name = 'Notice.AAA6.ClaimantIntent.PartAdmit.Defendant' and notification_role = 'DEFENDANT';
