/**
 * Update notification template
 */
UPDATE dbs.dashboard_notifications_templates
SET description_En = '<p class="govuk-body">${respondent1PartyName} has said that they will pay you ${defendantAdmittedAmount},plus the claim fee,immediately in full and final settlement of your claim and you have accepted this offer. Funds must be received in your account by ${respondent1AdmittedAmountPaymentDeadlineEn}.</p> <p class="govuk-body">If you don''t receive the money by then, you can <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">request a County Court Judgment(CCJ)</a>.</p>',
    description_Cy = '<p class="govuk-body">Mae ${respondent1PartyName} wedi dweud y byddant yn talu ${defendantAdmittedAmount},ynghyd â ffi’r hawliad,i chi ar unwaith fel setliad llawn a therfynol o’ch hawliad ac rydych wedi derbyn y cynnig hwn. Rhaid i’r arian gyrraedd eich cyfrif erbyn ${respondent1AdmittedAmountPaymentDeadlineCy}.</p> <p class="govuk-body">Os nad ydych wedi cael yr arian erbyn hynny, gallwch <a href={COUNTY_COURT_JUDGEMENT_URL} class="govuk-link">wneud cais am Ddyfarniad Llys Sifil (CCJ)</a>.</p>'
WHERE template_name = 'Notice.AAA6.ClaimantIntent.PartAdmit.Claimant' and notification_role = 'CLAIMANT';
